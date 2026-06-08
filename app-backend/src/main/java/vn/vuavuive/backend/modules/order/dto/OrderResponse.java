package vn.vuavuive.backend.modules.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO phản hồi đơn hàng — Trả về App Android sau khi tạo đơn hoặc xem đơn.
 * Nếu paymentMethod là VNPAY/MOMO, trường paymentUrl sẽ có giá trị để App mở WebView.
 */
public record OrderResponse(
        UUID orderId,
        String status,
        String paymentMethod,
        String paymentStatus,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        String deliveryAddress,
        String note,
        List<OrderItemResponse> items,
        List<OrderStatusLogResponse> statusTimeline,
        LocalDateTime createdAt,
        /** URL thanh toán VNPay/MoMo — null nếu là COD */
        String paymentUrl
) {}
