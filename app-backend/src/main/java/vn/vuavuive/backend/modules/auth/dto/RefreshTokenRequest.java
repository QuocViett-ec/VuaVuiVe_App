package vn.vuavuive.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO cho API làm mới Access Token bằng Refresh Token */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token không được để trống")
        String refreshToken
) {}
