package vn.vuavuive.backend.modules.payment.dto;

import java.math.BigDecimal;

public record CreateMomoPaymentResponse(
        String orderId,
        String requestId,
        BigDecimal amount,
        String payUrl,
        String deeplink,
        String qrCodeUrl,
        Integer resultCode,
        String message
) {}
