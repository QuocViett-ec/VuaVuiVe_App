package vn.vuavuive.backend.modules.payment.dto;

public record MomoCreateResponse(
        String partnerCode,
        String orderId,
        String requestId,
        Long amount,
        Long responseTime,
        String message,
        Integer resultCode,
        String payUrl,
        String deeplink,
        String qrCodeUrl
) {}
