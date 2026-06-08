package vn.vuavuive.backend.modules.category.dto;

import java.util.UUID;

/** DTO hiển thị thông tin danh mục trả về cho App Android */
public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String imageUrl,
        UUID parentId,
        String parentName
) {}
