package vn.vuavuive.backend.modules.payment.dto;

import java.math.BigDecimal;

public record CreateZaloPayPaymentResponse(
        String orderId,
        String appTransId,
        BigDecimal amount,
        String orderUrl,
        String zpTransToken,
        String qrCodeUrl,
        Integer returnCode,
        String returnMessage
) {}
