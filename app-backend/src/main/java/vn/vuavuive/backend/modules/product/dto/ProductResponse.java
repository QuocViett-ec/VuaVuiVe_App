package vn.vuavuive.backend.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/** DTO hiển thị thông tin sản phẩm — Dùng cho danh sách và chi tiết */
public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal originalPrice,
        BigDecimal sellingPrice,
        Integer stockQuantity,
        String unit,
        String imageUrl,
        Boolean isActive,
        UUID categoryId,
        String categoryName,
        /** % giảm giá tính toán từ original/selling price */
        Integer discountPercent
) {
    /** Alias getters for app compatibility */
    @JsonProperty("price")
    public BigDecimal price() {
        return sellingPrice;
    }

    @JsonProperty("stock")
    public Integer stock() {
        return stockQuantity;
    }
}
