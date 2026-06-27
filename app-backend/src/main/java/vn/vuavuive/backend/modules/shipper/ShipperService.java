package vn.vuavuive.backend.modules.shipper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.order.OrderRepository;
import vn.vuavuive.backend.modules.order.OrderStatusLog;
import vn.vuavuive.backend.modules.order.OrderStatusLogRepository;
import vn.vuavuive.backend.modules.shipper.dto.ShipperRequest;
import vn.vuavuive.backend.modules.shipper.dto.ShipperResponse;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vn.vuavuive.backend.modules.product.Product;
import vn.vuavuive.backend.modules.product.ProductRepository;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipperService {

    private final ShipperRepository shipperRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;

    /**
     * Tạo mới shipper (Admin)
     */
    @Transactional
    public ShipperResponse createShipper(ShipperRequest request) {
        if (shipperRepository.existsByPhone(request.phone())) {
            throw AppException.conflict("Số điện thoại shipper đã tồn tại!");
        }

        User user = userRepository.findByPhone(request.phone()).orElseGet(() -> {
            User newUser = User.builder()
                    .phone(request.phone())
                    .fullName(request.fullName())
                    .role(User.Role.SHIPPER)
                    .passwordHash(passwordEncoder.encode("shipper123"))
                    .isActive(true)
                    .build();
            return userRepository.save(newUser);
        });
        if (user.getRole() != User.Role.SHIPPER) {
            user.setRole(User.Role.SHIPPER);
            userRepository.save(user);
        }

        Shipper shipper = Shipper.builder()
                .fullName(request.fullName())
                .phone(request.phone())
                .vehicleNumber(request.vehicleNumber())
                .currentStatus(Shipper.Status.AVAILABLE)
                .isActive(true)
                .userId(user.getId())
                .build();

        return toResponse(shipperRepository.save(shipper));
    }
    /**
     * Lấy toàn bộ shipper
     */
    public List<ShipperResponse> getAllShippers() {
        return shipperRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ShipperResponse getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByPhone(email))
                .orElseThrow(() -> AppException.notFound("User"));
        Shipper shipper = shipperRepository.findByUserId(user.getId())
                .or(() -> shipperRepository.findByPhone(user.getPhone()))
                .orElseThrow(() -> AppException.notFound("Shipper profile chưa được thiết lập"));
        return toResponse(shipper);
    }

    /**
     * Cập nhật trạng thái hoạt động của Shipper (AVAILABLE, DELIVERING, OFFLINE)
     */
    @Transactional
    public ShipperResponse updateShipperStatus(String shipperId, String statusStr) {
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> AppException.notFound("Shipper"));

        Shipper.Status status = Shipper.Status.valueOf(statusStr.toUpperCase());
        shipper.setCurrentStatus(status);
        shipper = shipperRepository.save(shipper);

        // Thông báo cho Admin Dashboard về sự thay đổi trạng thái của Shipper
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("shipperId", shipperId);
        wsMessage.put("fullName", shipper.getFullName());
        wsMessage.put("status", status.name());
        messagingTemplate.convertAndSend("/topic/shippers/status", wsMessage);

        return toResponse(shipper);
    }

    /**
     * Admin gán đơn hàng cho Shipper.
     * Dùng PATCH để chỉ cập nhật các field cần thiết → Firebase Realtime listeners nhận event ngay lập tức.
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignShipperToOrder(String orderId, String shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> AppException.notFound("Shipper"));

        if (!shipper.getIsActive()) {
            throw AppException.badRequest("Shipper này hiện đang bị khóa tài khoản");
        }

        Order.OrderStatus currentStatus = order.getStatus();
        if (currentStatus != Order.OrderStatus.CONFIRMED
                && currentStatus != Order.OrderStatus.PREPARING
                && currentStatus != Order.OrderStatus.READY_FOR_PICKUP) {
            throw AppException.badRequest(
                    "Không thể gán shipper cho đơn đang ở trạng thái: " + currentStatus
                    + ". Chỉ gán được cho đơn CONFIRMED/PREPARING/READY_FOR_PICKUP.");
        }

        // Dùng PATCH để chỉ cập nhật các field thay đổi
        // → Firebase Realtime Database sẽ bắn event onChange ngay lập tức cho app mobile/web
        Map<String, Object> patch = new HashMap<>();
        patch.put("shipper_id", shipper.getId());
        patch.put("shipper_name", shipper.getFullName() != null ? shipper.getFullName() : shipper.getPhone());
        patch.put("status", "SHIPPING");
        orderRepository.patch(orderId, patch);

        // Cập nhật state trong memory để log
        order.setShipperId(shipper.getId());
        order.setShipperName(shipper.getFullName());
        order.setStatus(Order.OrderStatus.SHIPPING);

        // Lưu log lịch sử trạng thái
        appendStatusLog(order, Order.OrderStatus.SHIPPING,
                "Admin gán đơn hàng cho tài xế: " + (shipper.getFullName() != null ? shipper.getFullName() : shipper.getPhone()),
                "ADMIN", "Hệ thống");

        log.info("Gán shipper {} cho đơn hàng {} thành công", shipperId, orderId);

        // Gửi thông báo WebSocket tới Admin Dashboard
        notifyAdminDashboard(order, "Đã gán tài xế " + (shipper.getFullName() != null ? shipper.getFullName() : shipper.getPhone()) + " cho đơn hàng.");
    }

    /**
     * Shipper cập nhật tiến độ giao hàng.
     * - SHIPPING → IN_TRANSIT : Shipper đã nhận hàng, đang trên đường giao ("Đang giao")
     * - IN_TRANSIT → DELIVERED : Giao thành công
     * - IN_TRANSIT → FAILED    : Giao thất bại
     * - IN_TRANSIT → RETURNED  : Hoàn hàng
     *
     * Dùng PATCH để Firebase Realtime listeners nhận event ngay lập tức.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryStatus(String orderId, String shipperId, String newStatusStr, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> AppException.notFound("Shipper"));

        if (order.getShipperId() == null || !order.getShipperId().equals(shipperId)) {
            throw AppException.badRequest("Tài xế này không được phân công giao đơn hàng này!");
        }

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(newStatusStr.toUpperCase());
        Order.OrderStatus currentStatus = order.getStatus();

        // Định nghĩa các chuyển trạng thái hợp lệ
        boolean validTransition =
            // Shipper nhận hàng → Đang giao
            (newStatus == Order.OrderStatus.IN_TRANSIT
                && (currentStatus == Order.OrderStatus.SHIPPING
                 || currentStatus == Order.OrderStatus.CONFIRMED
                 || currentStatus == Order.OrderStatus.PREPARING
                 || currentStatus == Order.OrderStatus.READY_FOR_PICKUP))
            // Đang giao → Giao thành công / Thất bại
            || ((newStatus == Order.OrderStatus.DELIVERED || newStatus == Order.OrderStatus.FAILED)
                && currentStatus == Order.OrderStatus.IN_TRANSIT)
            // Đang giao → Hoàn hàng
            || (newStatus == Order.OrderStatus.RETURNED
                && (currentStatus == Order.OrderStatus.IN_TRANSIT || currentStatus == Order.OrderStatus.FAILED));

        if (!validTransition) {
            throw AppException.badRequest(
                "Không thể chuyển từ " + currentStatus + " sang " + newStatus
                + ". Chỉ được: SHIPPING→IN_TRANSIT, IN_TRANSIT→DELIVERED/FAILED/RETURNED");
        }

        // Dùng PATCH để cập nhật realtime Firebase
        Map<String, Object> patch = new HashMap<>();
        patch.put("status", newStatus.name());

        String shipperDisplayName = shipper.getFullName() != null ? shipper.getFullName() : shipper.getPhone();

        if (newStatus == Order.OrderStatus.DELIVERED) {
            // COD → đánh dấu đã thanh toán khi giao xong
            patch.put("payment_status", "PAID");
            shipper.setCurrentStatus(Shipper.Status.AVAILABLE);
        } else if (newStatus == Order.OrderStatus.FAILED || newStatus == Order.OrderStatus.RETURNED) {
            shipper.setCurrentStatus(Shipper.Status.AVAILABLE);
            // Hoàn lại tồn kho
            for (vn.vuavuive.backend.modules.order.OrderItem item : order.getOrderItems()) {
                if (item.getProductId() != null) {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    if (product != null) {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
            }
        } else if (newStatus == Order.OrderStatus.IN_TRANSIT) {
            // Shipper nhận đơn → chuyển sang "đang giao"
            shipper.setCurrentStatus(Shipper.Status.DELIVERING);
        }

        orderRepository.patch(orderId, patch);
        shipperRepository.save(shipper);

        // Cập nhật memory để ghi log
        order.setStatus(newStatus);
        appendStatusLog(order, newStatus,
                note != null && !note.isBlank() ? note : "Tài xế cập nhật trạng thái: " + newStatus.name(),
                "SHIPPER", shipperDisplayName);

        log.info("Shipper {} cập nhật đơn {} từ {} → {}", shipperId, orderId, currentStatus, newStatus);

        // Bắn WebSocket notification
        notifyAdminDashboard(order, "Tài xế " + shipperDisplayName + " → " + newStatus.name());
    }

    /**
     * Shipper gửi vị trí tọa độ của mình realtime (để Admin theo dõi trên bản đồ)
     */
    public void updateShipperLocation(String shipperId, double latitude, double longitude) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("shipperId", shipperId);
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("timestamp", System.currentTimeMillis());

        // Bắn trực tiếp tọa độ qua WebSocket topic
        messagingTemplate.convertAndSend("/topic/shippers/location", locationData);
        log.debug("Shipper {} cập nhật tọa độ: ({}, {})", shipperId, latitude, longitude);
    }

    // ================= Helpers =================

    private void appendStatusLog(Order order, Order.OrderStatus status, String note, String role, String updatedByName) {
        OrderStatusLog logEntity = OrderStatusLog.builder()
                .orderId(order.getId())
                .status(status)
                .note(note)
                .updatedByRole(role)
                .updatedByName(updatedByName)
                .build();
        statusLogRepository.save(logEntity);
    }

    private void notifyAdminDashboard(Order order, String message) {
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("orderId", order.getId());
        wsMessage.put("status", order.getStatus().name());
        wsMessage.put("paymentStatus", order.getPaymentStatus().name());
        wsMessage.put("message", message);
        wsMessage.put("updatedAt", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/admin/orders", wsMessage);
    }

    private ShipperResponse toResponse(Shipper s) {
        return new ShipperResponse(
                s.getId(),
                s.getFullName(),
                s.getPhone(),
                s.getVehicleNumber(),
                s.getCurrentStatus().name(),
                s.getIsActive(),
                s.getUserId()
        );
    }
}

