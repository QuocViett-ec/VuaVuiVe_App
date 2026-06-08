package vn.vuavuive.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO cho API Đăng nhập */
public record LoginRequest(

        @NotBlank(message = "Tài khoản không được để trống (email hoặc số điện thoại)")
        String identifier,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password
) {}
