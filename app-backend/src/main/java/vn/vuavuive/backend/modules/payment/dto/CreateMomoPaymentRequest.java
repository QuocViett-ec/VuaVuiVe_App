package vn.vuavuive.backend.modules.payment.dto;

import java.math.BigDecimal;

public record CreateMomoPaymentRequest(
        String orderId,
        BigDecimal amount,
        String orderInfo,
        String userId
) {}
