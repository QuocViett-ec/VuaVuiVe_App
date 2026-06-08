package vn.vuavuive.backend.modules.order.dto;

import java.time.LocalDateTime;

/** DTO một mốc lịch sử trạng thái — Hiển thị trên Timeline của Admin */
public record OrderStatusLogResponse(
        String status,
        String note,
        String updatedByName,
        String updatedByRole,
        LocalDateTime createdAt
) {}
