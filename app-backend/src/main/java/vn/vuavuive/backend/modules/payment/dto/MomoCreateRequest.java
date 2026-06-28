package vn.vuavuive.backend.modules.payment.dto;

public record MomoCreateRequest(
        String partnerCode,
        String accessKey,
        String requestId,
        String amount,
        String orderId,
        String orderInfo,
        String redirectUrl,
        String ipnUrl,
        String extraData,
        String requestType,
        String lang,
        String signature
) {}
