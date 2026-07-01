package vn.vuavuive.backend.modules.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO phản hồi đơn hàng — Trả về App Android sau khi tạo đơn hoặc xem đơn.
 * Nếu paymentMethod là MOMO/ZALOPAY, trường paymentUrl sẽ có giá trị để App mở WebView.
 * deliveryName & deliveryPhone được thêm vào để Shipper App hiển thị thông tin khách hàng
 * và kích hoạt nút Gọi điện (Quick Call) mà không cần gọi thêm API.
 */
public record OrderResponse(
        String orderId,
        String status,
        String paymentMethod,
        String paymentStatus,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        String deliveryAddress,
        /** Tên người nhận hàng */
        String deliveryName,
        /** Số điện thoại người nhận hàng — Dùng để Shipper gọi điện */
        String deliveryPhone,
        String note,
        List<OrderItemResponse> items,
        List<OrderStatusLogResponse> statusTimeline,
        LocalDateTime createdAt,
        /** URL thanh toán MoMo/ZaloPay — null nếu là COD */
        String paymentUrl
) {}
