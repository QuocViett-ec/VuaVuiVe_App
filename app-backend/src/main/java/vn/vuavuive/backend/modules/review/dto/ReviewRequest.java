package vn.vuavuive.backend.modules.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReviewRequest(
        @NotNull(message = "productId không được để trống")
        UUID productId,

        @NotNull(message = "Số sao không được để trống")
        @Min(value = 1, message = "Tối thiểu 1 sao")
        @Max(value = 5, message = "Tối đa 5 sao")
        Integer rating,

        @Size(max = 500, message = "Nhận xét tối đa 500 ký tự")
        String comment
) {}
