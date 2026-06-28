package vn.vuavuive.backend.modules.payment.dto;

import java.math.BigDecimal;

public record CreateZaloPayPaymentRequest(
        String orderId,
        BigDecimal amount,
        String description
) {}
