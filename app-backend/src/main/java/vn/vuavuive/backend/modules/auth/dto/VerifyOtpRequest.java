package vn.vuavuive.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
        @NotBlank(message = "Số điện thoại không được để trống")
        String phone,

        @NotBlank(message = "Mã OTP không được để trống")
        String code
) {}
