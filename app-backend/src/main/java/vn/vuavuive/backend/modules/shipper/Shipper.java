package vn.vuavuive.backend.modules.shipper;

import jakarta.persistence.*;
import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Bảng SHIPPERS — Lưu thông tin tài xế giao hàng.
 * Một Shipper có thể được gán vào nhiều đơn hàng theo thời gian (1 lúc chỉ 1 đơn).
 */
@Entity
@Table(name = "shippers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipper extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    /**
     * Trạng thái hiện tại của Shipper để hệ thống phân công đơn:
     * AVAILABLE  — Đang rảnh, có thể nhận đơn mới
     * DELIVERING — Đang giao hàng
     * OFFLINE    — Không hoạt động
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    @Builder.Default
    private Status currentStatus = Status.OFFLINE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum Status {
        AVAILABLE, DELIVERING, OFFLINE
    }
}
