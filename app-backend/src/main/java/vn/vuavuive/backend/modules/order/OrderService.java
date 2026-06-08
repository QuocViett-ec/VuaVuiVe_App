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
import vn.vuavuive.backend.modules.payment.VNPayService;
import vn.vuavuive.backend.modules.product.Product;
import vn.vuavuive.backend.modules.product.ProductRepository;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final VNPayService vnPayService;
    private final MoMoService moMoService;

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
     * 7. Nếu VNPAY/MOMO: Tạo URL thanh toán trả về cho App
     * 8. Nếu lỗi bất kỳ bước nào → toàn bộ ROLLBACK
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(CreateOrderRequest request, String clientIp) {
        // Lấy email từ JWT Token (đã được Spring Security xác thực)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User"));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Kiểm tra tồn kho và tính tiền từng sản phẩm
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> AppException.notFound("Sản phẩm " + itemReq.productId()));

            if (!product.getIsActive()) {
                throw AppException.badRequest("Sản phẩm '" + product.getName() + "' hiện không có bán");
            }

            // Kiểm tra tồn kho — Đây là bước quan trọng tránh oversell
            if (product.getStockQuantity() < itemReq.quantity()) {
                throw AppException.badRequest(
                        "Sản phẩm '" + product.getName() + "' chỉ còn "
                        + product.getStockQuantity() + " " + product.getUnit());
            }

            // Trừ tồn kho ngay lập tức (trong transaction)
            product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());
            productRepository.save(product);

            BigDecimal subtotal = product.getSellingPrice()
                    .multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(product.getSellingPrice())
                    .subtotal(subtotal)
                    .build();
            orderItems.add(orderItem);
        }

        // Tạo Order
        Order order = Order.builder()
                .user(user)
                .paymentMethod(request.paymentMethod())
                .totalAmount(totalAmount)
                .finalAmount(totalAmount) // Có thể trừ voucher ở đây sau
                .deliveryAddress(request.deliveryAddress())
                .note(request.note())
                .build();

        order = orderRepository.save(order);

        // Gán order_id cho từng item
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.getOrderItems().addAll(orderItems);
        order = orderRepository.save(order);

        // Ghi log trạng thái PENDING ban đầu
        appendStatusLog(order, Order.OrderStatus.PENDING, "Đơn hàng vừa được tạo", "SYSTEM", "Hệ thống");

        // Tạo URL thanh toán nếu cần
        String paymentUrl = null;
        if ("VNPAY".equals(request.paymentMethod())) {
            paymentUrl = vnPayService.createPaymentUrl(
                    order.getId().toString(), order.getFinalAmount(), clientIp);
            log.info("Tạo VNPay URL cho đơn {}", order.getId());
        } else if ("MOMO".equals(request.paymentMethod())) {
            paymentUrl = moMoService.createPaymentUrl(
                    order.getId().toString(), order.getFinalAmount());
            log.info("Tạo MoMo URL cho đơn {}", order.getId());
        }

        log.info("Đơn hàng {} đã được tạo bởi user {}", order.getId(), email);
        return toResponse(order, paymentUrl);
    }

    /**
     * Xem lịch sử đơn hàng của User hiện tại.
     */
    public PagedResponse<OrderResponse> getMyOrders(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User"));

        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return new PagedResponse<>(
                orders.getContent().stream().map(o -> toResponse(o, null)).toList(),
                orders.getNumber(), orders.getTotalPages(),
                orders.getTotalElements(), orders.isFirst(), orders.isLast()
        );
    }

    /**
     * Xem chi tiết một đơn hàng.
     */
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));
        return toResponse(order, null);
    }

    /**
     * Khách hàng HỦY đơn hàng (chỉ khi còn ở trạng thái PENDING).
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(UUID orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        // Chỉ cho phép hủy khi còn PENDING
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw AppException.badRequest(
                    "Không thể hủy đơn đang ở trạng thái " + order.getStatus());
        }

        // Hoàn lại tồn kho
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        appendStatusLog(order, Order.OrderStatus.CANCELLED, "Khách hàng hủy đơn", "CUSTOMER", email);
        return toResponse(orderRepository.save(order), null);
    }

    /**
     * Admin/Staff cập nhật trạng thái đơn hàng (CONFIRMED, PREPARING, READY_FOR_PICKUP...).
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse updateOrderStatus(UUID orderId, String newStatus, String note, String updatedByName) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        Order.OrderStatus status = Order.OrderStatus.valueOf(newStatus.toUpperCase());
        order.setStatus(status);
        appendStatusLog(order, status, note, "ADMIN", updatedByName);
        return toResponse(orderRepository.save(order), null);
    }

    /**
     * Xử lý khi VNPay IPN gọi về — Cập nhật trạng thái thanh toán.
     * Được gọi từ PaymentController khi VNPay xác nhận thanh toán.
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleVNPayIpn(String orderId, String responseCode) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        // 1. Nếu đơn đã thanh toán trước đó (trùng lặp IPN) -> Bỏ qua
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            log.info("VNPay IPN: Đơn {} đã được cập nhật thanh toán trước đó. Bỏ qua.", orderId);
            return;
        }

        // 2. Nếu đơn hàng đã bị hủy trước đó (do hết hạn hoặc khách hủy)
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            if ("00".equals(responseCode)) {
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                orderRepository.save(order);
                appendStatusLog(order, Order.OrderStatus.CANCELLED,
                        "Nhận thanh toán thành công từ VNPay nhưng đơn hàng đã bị hủy trước đó.", "SYSTEM", "VNPay Gateway");
                log.warn("VNPay IPN: Đơn {} đã bị hủy trước đó nhưng lại nhận được thanh toán thành công.", orderId);
            } else {
                log.info("VNPay IPN: Đơn {} đã bị hủy từ trước và giao dịch thanh toán thất bại. Bỏ qua.", orderId);
            }
            return;
        }

        // 3. Xử lý cho đơn hàng PENDING bình thường
        if ("00".equals(responseCode)) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            appendStatusLog(order, Order.OrderStatus.CONFIRMED,
                    "Thanh toán VNPay thành công", "SYSTEM", "VNPay Gateway");
            log.info("VNPay IPN: Đơn {} thanh toán thành công", orderId);
        } else {
            order.setStatus(Order.OrderStatus.CANCELLED);
            // Hoàn lại tồn kho khi thanh toán thất bại
            for (OrderItem item : order.getOrderItems()) {
                item.getProduct().setStockQuantity(
                        item.getProduct().getStockQuantity() + item.getQuantity());
                productRepository.save(item.getProduct());
            }
            appendStatusLog(order, Order.OrderStatus.CANCELLED,
                    "Thanh toán VNPay thất bại (mã lỗi: " + responseCode + ")", "SYSTEM", "VNPay Gateway");
            log.warn("VNPay IPN: Đơn {} thanh toán THẤT BẠI, responseCode={}", orderId, responseCode);
        }
        orderRepository.save(order);
    }

    /**
     * Xử lý MoMo IPN — Tương tự VNPay, resultCode=0 là thành công.
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
            order.setStatus(Order.OrderStatus.CONFIRMED);
            appendStatusLog(order, Order.OrderStatus.CONFIRMED,
                    "Thanh toán MoMo thành công", "SYSTEM", "MoMo Gateway");
        } else {
            order.setStatus(Order.OrderStatus.CANCELLED);
            for (OrderItem item : order.getOrderItems()) {
                item.getProduct().setStockQuantity(
                        item.getProduct().getStockQuantity() + item.getQuantity());
                productRepository.save(item.getProduct());
            }
            appendStatusLog(order, Order.OrderStatus.CANCELLED,
                    "Thanh toán MoMo thất bại (resultCode: " + resultCode + ")", "SYSTEM", "MoMo Gateway");
        }
        orderRepository.save(order);
    }

    // =================== Helpers ===================

    /** Ghi một mốc lịch sử trạng thái vào bảng ORDER_STATUS_LOGS */
    private void appendStatusLog(Order order, Order.OrderStatus status,
                                  String note, String role, String updatedByName) {
        OrderStatusLog log = OrderStatusLog.builder()
                .order(order)
                .status(status)
                .note(note)
                .updatedByRole(role)
                .updatedByName(updatedByName)
                .build();
        statusLogRepository.save(log);
    }

    /** Convert Order Entity -> OrderResponse DTO */
    private OrderResponse toResponse(Order order, String paymentUrl) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream().map(item ->
                new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getUnit(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                )).toList();

        List<OrderStatusLogResponse> logResponses = order.getStatusLogs().stream().map(l ->
                new OrderStatusLogResponse(
                        l.getStatus().name(),
                        l.getNote(),
                        l.getUpdatedByName(),
                        l.getUpdatedByRole(),
                        l.getCreatedAt()
                )).toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getPaymentMethod(),
                order.getPaymentStatus().name(),
                order.getTotalAmount(),
                order.getFinalAmount(),
                order.getDeliveryAddress(),
                order.getNote(),
                itemResponses,
                logResponses,
                order.getCreatedAt(),
                paymentUrl
        );
    }
}
