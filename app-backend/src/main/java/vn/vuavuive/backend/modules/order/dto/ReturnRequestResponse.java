package vn.vuavuive.backend.modules.order.dto;

public record ReturnRequestResponse(
        String reason,
        String status,
        String adminNote,
        String requestedAt
) {}
