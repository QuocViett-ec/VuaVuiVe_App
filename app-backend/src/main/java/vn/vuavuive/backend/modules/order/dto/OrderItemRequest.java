package vn.vuavuive.backend.modules.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** DTO cho từng sản phẩm trong đơn hàng */
public record OrderItemRequest(

        @NotNull(message = "ID sản phẩm không được để trống")
        UUID productId,

        @Min(value = 1, message = "Số lượng phải ít nhất là 1")
        int quantity
) {}
