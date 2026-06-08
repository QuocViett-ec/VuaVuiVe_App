package vn.vuavuive.backend.modules.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/** DTO tạo / cập nhật sản phẩm (Admin) */
public record ProductRequest(

        @NotBlank(message = "Tên sản phẩm không được để trống")
        String name,

        String description,

        @NotNull(message = "Giá gốc không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "Giá gốc phải lớn hơn 0")
        BigDecimal originalPrice,

        @NotNull(message = "Giá bán không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải lớn hơn 0")
        BigDecimal sellingPrice,

        @NotNull(message = "Số lượng tồn kho không được để trống")
        @Min(value = 0, message = "Tồn kho không được âm")
        Integer stockQuantity,

        /** KG, BUNCH, BOX, PIECE */
        @NotBlank(message = "Đơn vị tính không được để trống")
        String unit,

        String imageUrl,

        @NotNull(message = "Danh mục không được để trống")
        UUID categoryId
) {}
