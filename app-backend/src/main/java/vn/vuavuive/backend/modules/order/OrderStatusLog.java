package vn.vuavuive.backend.modules.order;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.vuavuive.backend.modules.order.Order.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng ORDER_STATUS_LOGS — Nhật ký lịch sử thay đổi trạng thái đơn hàng.
 *
 * Đây là bảng cốt lõi cho tính năng "Timeline Trạng Thái" trên màn hình Admin.
 * Mỗi khi đơn chuyển trạng thái (bởi Admin, Staff, hoặc Shipper),
 * một record mới được insert vào đây — KHÔNG xóa record cũ.
 *
 * Ví dụ hiển thị cho Admin:
 *   ✅ 14:00 - PENDING   (Tạo bởi: SYSTEM)
 *   ✅ 14:15 - CONFIRMED (Tạo bởi: ADMIN - Nguyễn Admin)
 *   ✅ 14:30 - IN_TRANSIT (Tạo bởi: SHIPPER - Trần Shipper B)
 *   ❌ 15:00 - FAILED   (Tạo bởi: SHIPPER - Ghi chú: "Gọi 3 lần không nghe máy")
 */
@Entity
@Table(name = "order_status_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Trạng thái mới vừa được áp dụng */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    /** Ghi chú giải thích lý do đổi trạng thái (bắt buộc cho FAILED/RETURNED) */
    @Column(name = "note")
    private String note;

    /** ID của người thực hiện thay đổi (Admin, Staff, hoặc Shipper) */
    @Column(name = "updated_by_id")
    private UUID updatedById;

    /** Tên người thực hiện (hiển thị cho Admin, không cần join) */
    @Column(name = "updated_by_name")
    private String updatedByName;

    /**
     * Vai trò của người thực hiện:
     * SYSTEM  — Hệ thống tự động (Cron Job hủy đơn)
     * ADMIN   — Quản trị viên
     * STAFF   — Nhân viên kho
     * SHIPPER — Tài xế giao hàng
     */
    @Column(name = "updated_by_role", nullable = false)
    private String updatedByRole;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
