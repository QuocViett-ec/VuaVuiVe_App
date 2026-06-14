package vn.vuavuive.backend.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal originalPrice,
        BigDecimal sellingPrice,
        Integer stockQuantity,
        String unit,
        String imageUrl,
        Boolean isActive,
        UUID categoryId,
        String categoryName,
        String category,
        String subCategory,
        List<String> tags,
        Integer externalId,
        Double rating,
        Integer reviewCount,
        Integer soldCount,
        Integer discountPercent
) {
    @JsonProperty("_id")
    public UUID legacyId() {
        return id;
    }

    @JsonProperty("price")
    public BigDecimal price() {
        return sellingPrice;
    }

    @JsonProperty("stock")
    public Integer stock() {
        return stockQuantity;
    }
}
