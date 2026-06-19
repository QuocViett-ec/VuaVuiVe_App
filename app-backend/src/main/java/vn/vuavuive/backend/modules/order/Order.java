package vn.vuavuive.backend.modules.order;

import jakarta.persistence.*;
import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;
import vn.vuavuive.backend.modules.shipper.Shipper;
import vn.vuavuive.backend.modules.user.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Bảng ORDERS — Đơn hàng của khách.
 * Quan hệ:
 *  - user_id     : Khách hàng tạo đơn
 *  - shipper_id  : Được điền khi Admin gán Shipper vào đơn
 *  - order_items : Chi tiết các sản phẩm trong đơn
 *  - status_logs : Toàn bộ lịch sử thay đổi trạng thái
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** null cho đến khi Admin gán Shipper */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private Shipper shipper;

    /**
     * Trạng thái đơn hàng — Tuân theo State Machine flow:
     * PENDING -> CONFIRMED -> PREPARING -> READY_FOR_PICKUP
     *   -> IN_TRANSIT -> DELIVERED (thành công)
     *              |
     *              +-> FAILED (giao thất bại, có thể thử lại)
     *              +-> RETURNED (khách từ chối / trả hàng)
     *              +-> CANCELLED (hủy đơn, hoàn tồn kho)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /** Tổng tiền trước voucher */
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /** Tổng tiền sau voucher (khách thực trả) */
    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // COD, VNPAY, MOMO

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    /** Địa chỉ giao hàng (lưu lại snapshot để không bị ảnh hưởng khi user đổi địa chỉ) */
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    /** Tên người nhận hàng — snapshot tại thời điểm đặt, tách riêng để dễ query & hiển thị */
    @Column(name = "delivery_name")
    private String deliveryName;

    /** Số điện thoại người nhận hàng — dùng để Shipper gọi điện */
    @Column(name = "delivery_phone", length = 20)
    private String deliveryPhone;

    @Column(name = "note")
    private String note;

    /** Chi tiết sản phẩm trong đơn */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    /** Lịch sử trạng thái — Timeline hiển thị cho Admin */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<OrderStatusLog> statusLogs = new ArrayList<>();

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        SHIPPING,
        PREPARING,
        READY_FOR_PICKUP,
        IN_TRANSIT,
        DELIVERED,
        FAILED,
        RETURNED,
        CANCELLED
    }

    public enum PaymentStatus {
        UNPAID, PENDING, PAID, FAILED, CANCELLED, REFUNDED
    }
}
