package vn.vuavuive.backend.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterOtpRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @JsonAlias("name")
        String fullName,

        String email,

        @NotBlank(message = "Số điện thoại không được để trống")
        String phone,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
        String password,

        String address
) {}
