package vn.vuavuive.backend.modules.payment;

import jakarta.persistence.*;
import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.user.User;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, unique = true)
    private String requestId;

    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String payUrl;
    @Column(columnDefinition = "TEXT")
    private String deeplink;
    @Column(columnDefinition = "TEXT")
    private String qrCodeUrl;
    private Integer resultCode;
    private String message;
    private Long responseTime;

    public enum Status {
        PENDING, PAID, FAILED, CANCELLED
    }
}
