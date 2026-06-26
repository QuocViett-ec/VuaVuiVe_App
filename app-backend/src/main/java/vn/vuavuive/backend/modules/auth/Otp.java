package vn.vuavuive.backend.modules.auth;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Lớp Otp — Thông tin mã xác thực OTP, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otp extends BaseEntity {

    private String phone;
    private String codeHash;
    private String type; // REGISTER, FORGOT_PASSWORD
    private String expiresAt;

    @Builder.Default
    private Boolean isUsed = false;

    @Builder.Default
    private Integer attemptCount = 0;

    private String lastSentAt;
    private String usedAt;
}
