package vn.vuavuive.backend.modules.payment.dto;

import java.math.BigDecimal;

public record PaymentStatusResponse(
        String orderId,
        String paymentMethod,
        String paymentStatus,
        String orderStatus,
        String transactionId,
        BigDecimal amount,
        String message
) {}
