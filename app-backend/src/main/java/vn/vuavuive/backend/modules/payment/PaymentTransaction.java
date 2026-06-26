package vn.vuavuive.backend.modules.payment;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Lớp PaymentTransaction — Nhật ký giao dịch thanh toán, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    private String orderId;
    private String userId;
    private String provider;
    private java.math.BigDecimal amount;
    private String requestId;
    private String transactionId;
    private Status status;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private Integer resultCode;
    private String message;
    private Long responseTime;

    public enum Status {
        PENDING, PAID, FAILED, CANCELLED
    }
}
