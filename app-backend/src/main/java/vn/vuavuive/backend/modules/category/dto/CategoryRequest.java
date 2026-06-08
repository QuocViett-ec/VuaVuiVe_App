package vn.vuavuive.backend.modules.category.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/** DTO tạo/cập nhật danh mục (Admin) */
public record CategoryRequest(
        @NotBlank(message = "Tên danh mục không được để trống")
        String name,

        String slug,         // Tự tạo từ name nếu để trống
        String imageUrl,
        UUID parentId        // null = danh mục gốc
) {}
