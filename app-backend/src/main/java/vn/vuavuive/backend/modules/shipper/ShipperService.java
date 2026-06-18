package vn.vuavuive.backend.modules.shipper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.order.OrderRepository;
import vn.vuavuive.backend.modules.order.OrderStatusLog;
import vn.vuavuive.backend.modules.order.OrderStatusLogRepository;
import vn.vuavuive.backend.modules.shipper.dto.ShipperRequest;
import vn.vuavuive.backend.modules.shipper.dto.ShipperResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipperService {

    private final ShipperRepository shipperRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Tạo mới shipper (Admin)
     */
    @Transactional
    public ShipperResponse createShipper(ShipperRequest request) {
        if (shipperRepository.existsByPhone(request.phone())) {
            throw AppException.conflict("Số điện thoại shipper đã tồn tại!");
        }

        Shipper shipper = Shipper.builder()
                .fullName(request.fullName())
                .phone(request.phone())
                .vehicleNumber(request.vehicleNumber())
                .currentStatus(Shipper.Status.AVAILABLE)
                .isActive(true)
                .build();

        return toResponse(shipperRepository.save(shipper));
    }

    /**
     * Lấy toàn bộ shipper
     */
    public List<ShipperResponse> getAllShippers() {
        return shipperRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Cập nhật trạng thái hoạt động của Shipper (AVAILABLE, DELIVERING, OFFLINE)
     */
    @Transactional
    public ShipperResponse updateShipperStatus(UUID shipperId, String statusStr) {
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
     * Admin gán đơn hàng cho Shipper
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignShipperToOrder(UUID orderId, UUID shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> AppException.notFound("Shipper"));

        if (!shipper.getIsActive()) {
            throw AppException.badRequest("Shipper này hiện đang bị khóa tài khoản");
        }

        if (order.getPaymentStatus() != Order.PaymentStatus.PAID) {
            throw AppException.badRequest("Chi gan shipper cho don hang da thanh toan");
        }

        order.setShipper(shipper);
        order.setStatus(Order.OrderStatus.SHIPPING); // Chuyển sang trạng thái chuẩn bị / chuẩn bị giao
        orderRepository.save(order);

        // Lưu log lịch sử trạng thái
        appendStatusLog(order, Order.OrderStatus.SHIPPING, 
                "Admin gán đơn hàng cho tài xế: " + shipper.getFullName(), 
                "ADMIN", "Hệ thống");

        // Gửi thông báo WebSocket tới Admin Dashboard
        notifyAdminDashboard(order, "Đã gán tài xế " + shipper.getFullName() + " cho đơn hàng.");
    }

    /**
     * Shipper cập nhật trạng thái đơn hàng (IN_TRANSIT, DELIVERED, FAILED, RETURNED)
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryStatus(UUID orderId, UUID shipperId, String newStatusStr, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> AppException.notFound("Shipper"));

        if (order.getShipper() == null || !order.getShipper().getId().equals(shipperId)) {
            throw AppException.badRequest("Tài xế này không được phân công giao đơn hàng này!");
        }

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(newStatusStr.toUpperCase());
        
        // Cập nhật trạng thái đơn
        order.setStatus(newStatus);

        // Nếu giao thành công
        if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setPaymentStatus(Order.PaymentStatus.PAID); // COD thanh toán thành công
            shipper.setCurrentStatus(Shipper.Status.AVAILABLE);
        } 
        // Nếu thất bại hoặc hoàn hàng
        else if (newStatus == Order.OrderStatus.FAILED || newStatus == Order.OrderStatus.RETURNED) {
            shipper.setCurrentStatus(Shipper.Status.AVAILABLE);
            // Hoàn lại tồn kho cho sản phẩm
            for (vn.vuavuive.backend.modules.order.OrderItem item : order.getOrderItems()) {
                item.getProduct().setStockQuantity(
                        item.getProduct().getStockQuantity() + item.getQuantity());
            }
        } 
        // Đang đi giao
        else if (newStatus == Order.OrderStatus.IN_TRANSIT) {
            shipper.setCurrentStatus(Shipper.Status.DELIVERING);
        }

        orderRepository.save(order);
        shipperRepository.save(shipper);

        // Ghi nhận lịch sử trạng thái đơn
        appendStatusLog(order, newStatus, note, "SHIPPER", shipper.getFullName());

        // Bắn thông báo Realtime qua WebSocket cho Admin Dashboard
        notifyAdminDashboard(order, "Tài xế " + shipper.getFullName() + " cập nhật trạng thái đơn hàng thành: " + newStatusStr);
    }

    /**
     * Shipper gửi vị trí tọa độ của mình realtime (để Admin theo dõi trên bản đồ)
     */
    public void updateShipperLocation(UUID shipperId, double latitude, double longitude) {
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
                .order(order)
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
                s.getIsActive()
        );
    }
}

