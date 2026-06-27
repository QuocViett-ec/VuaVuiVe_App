package vn.vuavuive.backend.modules.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        String productId,
        String productName,
        String productImageUrl,
        String unit,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
