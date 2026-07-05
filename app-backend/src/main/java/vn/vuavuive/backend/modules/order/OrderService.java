package vn.vuavuive.backend.modules.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.order.dto.*;
import vn.vuavuive.backend.modules.payment.MoMoService;
import vn.vuavuive.backend.modules.payment.ZaloPayService;
import vn.vuavuive.backend.modules.payment.dto.CreateMomoPaymentRequest;
import vn.vuavuive.backend.modules.payment.dto.CreateZaloPayPaymentRequest;
import vn.vuavuive.backend.modules.product.Product;
import vn.vuavuive.backend.modules.product.ProductRepository;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;
import vn.vuavuive.backend.modules.shipper.Shipper;
import vn.vuavuive.backend.modules.shipper.ShipperRepository;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * OrderService — Xử lý toàn bộ nghiệp vụ đặt hàng.
 *
 * Điểm mấu chốt: @Transactional(rollbackFor = Exception.class)
 * Khi bất kỳ bước nào lỗi (hết hàng, lỗi DB...), toàn bộ thay đổi
 * sẽ được tự động rollback để tránh trạng thái dữ liệu không nhất quán.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(30_000);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final ShipperRepository shipperRepository;
    private final MoMoService moMoService;
    private final ZaloPayService zaloPayService;

    /**
     * TẠO ĐƠN HÀNG — Đây là hàm quan trọng nhất, được bảo vệ bởi @Transactional.
     *
     * Các bước thực hiện trong 1 transaction:
     * 1. Load User hiện tại từ SecurityContext
     * 2. Kiểm tra từng sản phẩm: Có tồn tại? Còn hàng đủ không?
     * 3. Tính tổng tiền
     * 4. TRỪ tồn kho (pessimistic: khóa row để tránh race condition)
     * 5. Tạo Order và OrderItems trong DB
     * 6. Ghi Log trạng thái PENDING
     * 7. Nếu MOMO/ZALOPAY: Tạo URL thanh toán trả về cho App
     * 8. Nếu lỗi bất kỳ bước nào → toàn bộ ROLLBACK
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(CreateOrderRequest request, String clientIp) {
        // Lấy email từ JWT Token (đã được Spring Security xác thực)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByPhone(email))
                .orElseThrow(() -> AppException.notFound("User"));

        List<OrderItem> orderItems = new ArrayList<>();
        Map<String, Product> productsToUpdate = new HashMap<>();
        Map<String, Integer> quantitiesByProduct = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Kiểm tra tồn kho và tính tiền từng sản phẩm
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productsToUpdate.get(itemReq.productId());
            if (product == null) {
                product = productRepository.findById(itemReq.productId())
                        .orElseThrow(() -> AppException.notFound("Sản phẩm " + itemReq.productId()));
                productsToUpdate.put(itemReq.productId(), product);
            }

            if (!product.getIsActive()) {
                throw AppException.badRequest("Sản phẩm '" + product.getName() + "' hiện không có bán");
            }

            if (itemReq.quantity() <= 0) {
                throw AppException.badRequest("Số lượng sản phẩm phải lớn hơn 0");
            }

            // Kiểm tra tồn kho — Đây là bước quan trọng tránh oversell
            int requestedQuantity = quantitiesByProduct.getOrDefault(itemReq.productId(), 0) + itemReq.quantity();
            if (product.getStockQuantity() == null || product.getStockQuantity() < requestedQuantity) {
                throw AppException.badRequest(
                        "Sản phẩm '" + product.getName() + "' chỉ còn "
                        + product.getStockQuantity() + " " + product.getUnit());
            }
            quantitiesByProduct.put(itemReq.productId(), requestedQuantity);

            BigDecimal unitPrice = product.getSellingPrice();
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw AppException.badRequest("Sản phẩm '" + product.getName() + "' chưa có giá bán hợp lệ");
            }

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemReq.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
            orderItems.add(orderItem);
        }

        for (Map.Entry<String, Product> entry : productsToUpdate.entrySet()) {
            Product product = entry.getValue();
            product.setStockQuantity(product.getStockQuantity() - quantitiesByProduct.get(entry.getKey()));
            productRepository.save(product);
        }

        // Tạo Order
        BigDecimal shippingFee = DEFAULT_SHIPPING_FEE;
        BigDecimal discount = calculateDiscount(request.getVoucherCode(), totalAmount, shippingFee);
        BigDecimal finalAmount = totalAmount.add(shippingFee).subtract(discount).max(BigDecimal.ZERO);

        // Lấy thông tin giao hàng từ request.getDelivery() (đối tượng DeliveryInfo riêng)
        String recipientName  = request.getDelivery() != null ? request.getDelivery().name()  : null;
        String recipientPhone = request.getDelivery() != null ? request.getDelivery().phone() : null;

        Order order = Order.builder()
                .userId(user.getId())
                .userName(user.getFullName())
                .userPhone(user.getPhone())
                .paymentMethod(request.paymentMethod())
                .paymentStatus(initialPaymentStatus(request.paymentMethod()))
                .status(initialOrderStatus(request.paymentMethod()))
                .totalAmount(totalAmount)
                .shippingFee(shippingFee)
                .discount(discount)
                .finalAmount(finalAmount)
                .deliveryAddress(request.deliveryAddress())  // chuỗi tổng hợp "Tên (SĐT): Địa chỉ"
                .deliveryName(recipientName)                  // lưu riêng để dễ hiển thị
                .deliveryPhone(recipientPhone)                // lưu riêng để Shipper gọi điện
                .note(request.note())
                .build();

        order.getOrderItems().addAll(orderItems);
        order = orderRepository.save(order);

        // Ghi log trạng thái PENDING ban đầu
        appendStatusLog(order, order.getStatus(), "Đơn hàng vừa được tạo", "SYSTEM", "Hệ thống");

        // Tạo URL thanh toán nếu cần
        String paymentUrl = null;
        try {
            if ("MOMO".equals(request.paymentMethod())) {
                paymentUrl = moMoService.createMomoPayment(new CreateMomoPaymentRequest(
                        order.getId(),
                        order.getFinalAmount(),
                        "Thanh toan don hang Vua Vui Ve: " + order.getId(),
                        user.getId()
                )).payUrl();
                order = orderRepository.findById(order.getId()).orElse(order);
            } else if ("ZALOPAY".equals(request.paymentMethod())) {
                paymentUrl = zaloPayService.createZaloPayPayment(new CreateZaloPayPaymentRequest(
                        order.getId(),
                        order.getFinalAmount(),
                        "Thanh toan don hang Vua Vui Ve: " + order.getId()
                )).orderUrl();
                order = orderRepository.findById(order.getId()).orElse(order);
            }
        } catch (RuntimeException e) {
            log.warn("Khong tao duoc URL thanh toan cho don {}, app co the goi lai API payment", order.getId(), e);
        }

        log.info("Đơn hàng {} đã được tạo bởi user {}", order.getId(), email);
        return toResponse(order, paymentUrl);
    }

    /**
     * Xem lịch sử đơn hàng của User hiện tại.
     */
    public PagedResponse<OrderResponse> getMyOrders(String statusStr, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByPhone(email))
                .orElseThrow(() -> AppException.notFound("User"));

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Order.OrderStatus status = parseOrderStatus(statusStr);
        Page<Order> orders = status == null
                ? orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                : orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status, pageable);

        return new PagedResponse<>(
                orders.getContent().stream().map(o -> toResponse(o, null)).toList(),
                orders.getNumber(), orders.getTotalPages(),
                orders.getTotalElements(), orders.isFirst(), orders.isLast()
        );
    }

    public PagedResponse<OrderResponse> getAllOrders(String statusStr, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Order.OrderStatus status = parseOrderStatus(statusStr);
        Page<Order> orders = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        return new PagedResponse<>(
                orders.getContent().stream().map(o -> toResponse(o, null)).toList(),
                orders.getNumber(), orders.getTotalPages(),
                orders.getTotalElements(), orders.isFirst(), orders.isLast()
        );
    }

    /**
     * Xem chi tiết một đơn hàng.
     */
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));
        return toResponse(order, null);
    }

    /**
     * [SHIPPER] Lấy danh sách đơn hàng được gán cho Shipper hiện tại.
     * Tìm Shipper qua SĐT của User đang đăng nhập, sau đó truy vấn các đơn có shipperId tương ứng.
     */
    public PagedResponse<OrderResponse> getShipperOrders(String statusStr, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByPhone(email))
                .orElseThrow(() -> AppException.notFound("User"));

        // Tìm Shipper entity qua số điện thoại của User SHIPPER
        Shipper shipper = shipperRepository.findByUserId(user.getId())
                .or(() -> shipperRepository.findByPhone(user.getPhone()))
                .orElseThrow(() -> AppException.notFound("Thông tin tài xế chưa được thiết lập"));

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orders;
        if (statusStr == null || statusStr.isBlank()) {
            orders = orderRepository.findByShipperIdOrderByCreatedAtDesc(shipper.getId(), pageable);
        } else {
            Order.OrderStatus status = parseOrderStatus(statusStr);
            orders = orderRepository.findByShipperIdAndStatusOrderByCreatedAtDesc(
                    shipper.getId(), status, pageable);
        }

        return new PagedResponse<>(
                orders.getContent().stream().map(o -> toResponse(o, null)).toList(),
                orders.getNumber(), orders.getTotalPages(),
                orders.getTotalElements(), orders.isFirst(), orders.isLast()
        );
    }

    /**
     * Khách hàng HỦY đơn hàng (chỉ khi còn ở trạng thái PENDING).
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(String orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        if (!OrderStateMachine.canCustomerCancel(order.getStatus())) {
            throw AppException.badRequest(
                    "Không thể hủy đơn đang ở trạng thái " + order.getStatus());
        }

        restoreStockOnce(order);
        order.setStatus(Order.OrderStatus.CANCELLED);
        cancelUnpaidPayment(order);
        appendStatusLog(order, Order.OrderStatus.CANCELLED, "Khách hàng hủy đơn", "CUSTOMER", email);
        return toResponse(orderRepository.save(order), null);
    }

    /** Admin/Staff xác nhận hoặc hủy đơn hàng. */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse updateOrderStatus(String orderId, String newStatus, String note, String updatedByName) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        Order.OrderStatus status = parseOrderStatus(newStatus);
        if (status == null) {
            throw AppException.badRequest("Trang thai don hang khong hop le");
        }
        if (!OrderStateMachine.canAdminTransition(order.getStatus(), status)) {
            throw AppException.badRequest(
                    "Không thể chuyển từ " + order.getStatus() + " sang " + status);
        }
        if (status == Order.OrderStatus.CANCELLED) {
            restoreStockOnce(order);
            cancelUnpaidPayment(order);
        }
        order.setStatus(status);
        appendStatusLog(order, status, note, "ADMIN", updatedByName);
        return toResponse(orderRepository.save(order), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse requestReturn(String orderId, String reason) {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> AppException.notFound("User"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));
        if (!user.getId().equals(order.getUserId())) {
            throw AppException.forbidden("Bạn không có quyền thao tác đơn hàng này");
        }
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw AppException.badRequest("Chỉ có thể yêu cầu trả đơn đã giao");
        }
        Map<String, Object> current = order.getReturnRequest();
        if (current != null && "PENDING".equalsIgnoreCase(String.valueOf(current.get("status")))) {
            throw AppException.badRequest("Đơn hàng đã có yêu cầu trả đang chờ xử lý");
        }
        Map<String, Object> request = new HashMap<>();
        request.put("reason", reason);
        request.put("status", "PENDING");
        request.put("requested_at", java.time.Instant.now().toString());
        order.setReturnRequest(request);
        orderRepository.patch(orderId, Map.of("return_request", request));
        return toResponse(order, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse reviewReturnRequest(String orderId, String action, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));
        Map<String, Object> request = order.getReturnRequest();
        if (request == null || !"PENDING".equalsIgnoreCase(String.valueOf(request.get("status")))) {
            throw AppException.badRequest("Yêu cầu trả hàng không ở trạng thái chờ duyệt");
        }
        String status;
        if ("approve".equalsIgnoreCase(action)) {
            status = "APPROVED";
        } else if ("reject".equalsIgnoreCase(action)) {
            status = "REJECTED";
        } else {
            throw AppException.badRequest("Hành động duyệt trả hàng không hợp lệ");
        }
        request = new HashMap<>(request);
        request.put("status", status);
        request.put("admin_note", note == null ? "" : note);
        request.put("reviewed_at", java.time.Instant.now().toString());
        order.setReturnRequest(request);
        orderRepository.patch(orderId, Map.of("return_request", request));
        return toResponse(order, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse markPaid(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));
        if (!"COD".equalsIgnoreCase(order.getPaymentMethod())) {
            throw AppException.badRequest("Chỉ đánh dấu thanh toán thủ công cho đơn COD");
        }
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw AppException.badRequest("Chỉ đánh dấu đã thanh toán sau khi đơn COD đã giao thành công");
        }
        if (order.getPaymentStatus() == Order.PaymentStatus.REFUNDED) {
            throw AppException.badRequest("Đơn đã hoàn tiền không thể đánh dấu đã thanh toán");
        }
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            return toResponse(order, null);
        }
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        awardPointsForOrder(order);
        appendStatusLog(order, order.getStatus(), "Admin xác nhận COD đã thu tiền", "ADMIN", "Admin");
        return toResponse(orderRepository.save(order), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse markRefunded(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));
        if (order.getStatus() != Order.OrderStatus.RETURNED) {
            throw AppException.badRequest("Chỉ hoàn tiền sau khi đã nhận lại hàng");
        }
        order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        return toResponse(orderRepository.save(order), null);
    }

    /**
     * Xử lý MoMo IPN, resultCode=0 là thành công.
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleMoMoIpn(String orderId, String resultCode) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        // 1. Nếu đơn đã thanh toán trước đó (trùng lặp IPN) -> Bỏ qua
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            log.info("MoMo IPN: Đơn {} đã được cập nhật thanh toán trước đó. Bỏ qua.", orderId);
            return;
        }

        // 2. Nếu đơn hàng đã bị hủy trước đó (do hết hạn hoặc khách hủy)
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            if ("0".equals(resultCode)) {
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                orderRepository.save(order);
                appendStatusLog(order, Order.OrderStatus.CANCELLED,
                        "Nhận thanh toán thành công từ MoMo nhưng đơn hàng đã bị hủy trước đó.", "SYSTEM", "MoMo Gateway");
                log.warn("MoMo IPN: Đơn {} đã bị hủy trước đó nhưng lại nhận được thanh toán thành công.", orderId);
            } else {
                log.info("MoMo IPN: Đơn {} đã bị hủy từ trước và giao dịch thanh toán thất bại. Bỏ qua.", orderId);
            }
            return;
        }

        // 3. Xử lý cho đơn hàng PENDING bình thường
        if ("0".equals(resultCode)) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.PENDING_APPROVAL);
            appendStatusLog(order, Order.OrderStatus.PENDING_APPROVAL,
                    "Thanh toán MoMo thành công", "SYSTEM", "MoMo Gateway");
        } else {
            order.setStatus(Order.OrderStatus.CANCELLED);
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProductId() != null) {
                    Product product = productRepository.findById(UUID.fromString(item.getProductId())).orElse(null);
                    if (product != null) {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
            }
            appendStatusLog(order, Order.OrderStatus.CANCELLED,
                    "Thanh toán MoMo thất bại (resultCode: " + resultCode + ")", "SYSTEM", "MoMo Gateway");
        }
        orderRepository.save(order);
    }

    public void awardPointsForOrder(Order order) {
        if (order.getPointsAdded() == null || !order.getPointsAdded()) {
            User user = order.getUserId() != null ? userRepository.findById(order.getUserId()).orElse(null) : null;
            if (user != null) {
                BigDecimal totalAmount = order.getTotalAmount();
                if (totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // Mechanism: 10,000đ = 10 points. e.g. 152,000đ -> 150 points.
                    // Math: (totalAmount / 10000) * 10
                    int earnedPoints = totalAmount.divide(BigDecimal.valueOf(10000), 0, java.math.RoundingMode.DOWN).intValue() * 10;
                    if (earnedPoints > 0) {
                        user.setPoints(user.getPoints() + earnedPoints);
                        userRepository.save(user);
                        log.info("Awarded {} points to user {} for order {}", earnedPoints, user.getEmail() != null ? user.getEmail() : user.getPhone(), order.getId());
                    }
                }
            }
            order.setPointsAdded(true);
            orderRepository.save(order);
        }
    }

    // =================== Helpers ===================

    private Order.OrderStatus parseOrderStatus(String value) {
        if (value == null || value.isBlank() || "all".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Order.OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Order.PaymentStatus initialPaymentStatus(String paymentMethod) {
        return isOnlinePayment(paymentMethod) ? Order.PaymentStatus.PENDING : Order.PaymentStatus.UNPAID;
    }

    public boolean restoreStockOnce(Order order) {
        if (Boolean.TRUE.equals(order.getStockRestored())) return false;
        for (OrderItem item : order.getOrderItems()) {
            if (item.getProductId() == null) continue;
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
        order.setStockRestored(true);
        return true;
    }

    private Order.OrderStatus initialOrderStatus(String paymentMethod) {
        if (isOnlinePayment(paymentMethod)) {
            return Order.OrderStatus.PENDING_PAYMENT;
        }
        if ("COD".equalsIgnoreCase(paymentMethod)) {
            return Order.OrderStatus.PENDING_APPROVAL;
        }
        return Order.OrderStatus.PENDING_APPROVAL;
    }

    private boolean isOnlinePayment(String paymentMethod) {
        return "MOMO".equalsIgnoreCase(paymentMethod)
                || "ZALOPAY".equalsIgnoreCase(paymentMethod);
    }

    private BigDecimal calculateDiscount(String voucherCode, BigDecimal subtotal, BigDecimal shippingFee) {
        if (voucherCode == null || voucherCode.isBlank()) return BigDecimal.ZERO;
        String code = voucherCode.trim().toUpperCase();
        if ("VUAVUIVE".equals(code)) {
            return subtotal.multiply(BigDecimal.valueOf(15)).divide(BigDecimal.valueOf(100));
        }
        if ("FREESHIP24".equals(code) || "FREESHIP".equals(code)) {
            return shippingFee;
        }
        return BigDecimal.ZERO;
    }

    private void cancelUnpaidPayment(Order order) {
        if (order.getPaymentStatus() != Order.PaymentStatus.PAID
                && order.getPaymentStatus() != Order.PaymentStatus.REFUNDED) {
            order.setPaymentStatus(Order.PaymentStatus.CANCELLED);
        }
    }

    /** Ghi một mốc lịch sử trạng thái vào bảng ORDER_STATUS_LOGS */
    private void appendStatusLog(Order order, Order.OrderStatus status,
                                  String note, String role, String updatedByName) {
        OrderStatusLog log = OrderStatusLog.builder()
                .orderId(order.getId())
                .status(status)
                .note(note)
                .updatedByRole(role)
                .updatedByName(updatedByName)
                .build();
        statusLogRepository.save(log);
    }

    /** Convert Order Entity -> OrderResponse DTO */
    private OrderResponse toResponse(Order order, String paymentUrl) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream().map(item -> {
            Product product = item.getProductId() != null ? productRepository.findById(item.getProductId()).orElse(null) : null;
            return new OrderItemResponse(
                    item.getProductId() != null ? item.getProductId() : null,
                    item.getProductName(),
                    product != null ? product.getImageUrl() : null,
                    product != null ? product.getUnit() : "KG",
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
            );
        }).toList();

        // Tránh lazy-loading status_logs
        List<OrderStatusLogResponse> logResponses = List.of();

        // Ưu tiên 1: đọc từ cột riêng delivery_name / delivery_phone (đơn hàng mới sau migration)
        String deliveryName  = order.getDeliveryName();
        String deliveryPhone = order.getDeliveryPhone();
        String deliveryAddr  = order.getDeliveryAddress();

        // Ưu tiên 2: parse từ chuỗi tổng hợp "Tên (SĐT): Địa chỉ" (đơn hàng cũ trước migration)
        if ((deliveryName == null || deliveryName.isBlank()) && deliveryAddr != null
                && deliveryAddr.contains(" (") && deliveryAddr.contains("): ")) {
            try {
                int nameEnd  = deliveryAddr.indexOf(" (");
                int phoneEnd = deliveryAddr.indexOf("): ");
                if (nameEnd > 0 && phoneEnd > nameEnd) {
                    deliveryName  = deliveryAddr.substring(0, nameEnd).trim();
                    deliveryPhone = deliveryAddr.substring(nameEnd + 2, phoneEnd).trim();
                    deliveryAddr  = deliveryAddr.substring(phoneEnd + 3).trim();
                }
            } catch (Exception ignored) {}
        }

        // Ưu tiên 3 (fallback): lấy từ User account — chỉ cho đơn rất cũ không có format nào
        if (deliveryName == null && order.getUserId() != null) {
            User user = userRepository.findById(order.getUserId()).orElse(null);
            if (user != null) {
                deliveryName  = user.getFullName();
                deliveryPhone = user.getPhone();
            }
        }

        // Dùng biến parsedXxx để tương thích với return bên dưới
        String parsedName    = deliveryName;
        String parsedPhone   = deliveryPhone;
        String parsedAddress = deliveryAddr;

        return new OrderResponse(
                order.getId(),
                order.getShipperId(),
                order.getStatus().name(),
                order.getPaymentMethod(),
                order.getPaymentStatus().name(),
                order.getTotalAmount(),
                amountOrZero(order.getShippingFee()),
                amountOrZero(order.getDiscount()),
                order.getFinalAmount(),
                parsedAddress,
                parsedName,
                parsedPhone,
                order.getNote(),
                toReturnRequestResponse(order.getReturnRequest()),
                itemResponses,
                logResponses,
                order.getCreatedAt() != null ? LocalDateTime.ofInstant(java.time.Instant.parse(order.getCreatedAt()), java.time.ZoneId.systemDefault()) : null,
                paymentUrl
        );
    }

    private BigDecimal amountOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private ReturnRequestResponse toReturnRequestResponse(Map<String, Object> request) {
        if (request == null || request.isEmpty()) return null;
        return new ReturnRequestResponse(
                stringValue(request, "reason"),
                stringValue(request, "status"),
                stringValue(request, "admin_note", "adminNote"),
                stringValue(request, "requested_at", "requestedAt")
        );
    }

    private String stringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) return String.valueOf(value);
        }
        return null;
    }
}

