package vn.vuavuive.backend.modules.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import vn.vuavuive.backend.exception.AppException;

import java.math.BigDecimal;
import java.util.UUID;

/** DTO cho từng sản phẩm trong đơn hàng */
public record OrderItemRequest(

        @NotNull(message = "ID sản phẩm không được để trống")
        String productId,

        @Min(value = 1, message = "Số lượng phải ít nhất là 1")
        int quantity,

        BigDecimal price
) {
    public UUID productUuid() {
        try {
            String raw = productId == null ? null : productId.trim();
            if (raw == null || raw.isEmpty()) {
                throw AppException.badRequest("ID sản phẩm không hợp lệ");
            }
            if (raw.startsWith("prod-")) {
                raw = raw.substring(5);
            }
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("ID sản phẩm không hợp lệ: " + productId);
        }
    }
}
