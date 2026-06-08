package vn.vuavuive.backend.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import vn.vuavuive.backend.modules.user.User;

public record UserResponse(
    @JsonProperty("_id") String id,
    String name,
    String phone,
    String email,
    String avatar,
    String provider,
    String address,
    String role,
    boolean isActive,
    String createdAt,
    String updatedAt
) {
    public static UserResponse fromEntity(User user) {
        String roleStr = user.getRole().name().toLowerCase();
        // Map CUSTOMER to "user", ADMIN to "admin", STAFF to "staff", SHIPPER to "shipper" (or "user" if needed, wait, let's keep shipper as "shipper")
        if ("customer".equals(roleStr)) {
            roleStr = "user";
        }
        return new UserResponse(
            user.getId().toString(),
            user.getFullName(),
            user.getPhone(),
            user.getEmail(),
            user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
            "local",
            "",
            roleStr,
            user.getIsActive(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
            user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null
        );
    }
}
