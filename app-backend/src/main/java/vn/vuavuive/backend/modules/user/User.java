package vn.vuavuive.backend.modules.user;

import jakarta.persistence.*;
import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Bảng USERS — Lưu thông tin người dùng (Khách hàng, Admin, Staff)
 * Một User có thể có nhiều địa chỉ giao hàng (ADDRESSES) và nhiều đơn hàng (ORDERS).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.CUSTOMER;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "points")
    @Builder.Default
    private Integer points = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Phân quyền trong hệ thống:
     * CUSTOMER  — Khách hàng mua hàng thông thường
     * STAFF     — Nhân viên kho, quản lý đơn hàng
     * ADMIN     — Quản trị viên toàn quyền
     */
    public enum Role {
        CUSTOMER, STAFF, ADMIN, SHIPPER
    }
}
