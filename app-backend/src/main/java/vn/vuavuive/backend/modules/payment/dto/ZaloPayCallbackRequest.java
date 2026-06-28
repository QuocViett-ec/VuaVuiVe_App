package vn.vuavuive.backend.modules.payment.dto;

public record ZaloPayCallbackRequest(
        String data,
        String mac,
        Integer type
) {}
