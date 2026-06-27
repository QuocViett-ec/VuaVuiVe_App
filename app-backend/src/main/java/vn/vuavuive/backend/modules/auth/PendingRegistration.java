package vn.vuavuive.backend.modules.auth;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Lớp PendingRegistration — Lưu thông tin đăng ký chờ xác thực OTP, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegistration extends BaseEntity {

    private String phone;
    private String email;
    private String fullName;
    private String passwordHash;
    private String address;
    private String expiresAt;
}
