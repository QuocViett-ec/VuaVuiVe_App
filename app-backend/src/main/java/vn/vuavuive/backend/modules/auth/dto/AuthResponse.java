package vn.vuavuive.backend.modules.auth.dto;

import java.util.UUID;

/**
 * DTO phản hồi khi đăng nhập/đăng ký thành công.
 * App Android lưu accessToken và refreshToken lại để dùng tiếp.
 */
public record AuthResponse(
        UUID userId,
        String fullName,
        String email,
        String role,
        String accessToken,     // Tuổi thọ: 15 phút — Dùng gọi API
        String refreshToken     // Tuổi thọ: 30 ngày — Lưu vào SharedPreferences/DataStore
) {}
