package vn.vuavuive.backend.modules.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import vn.vuavuive.backend.modules.review.Review;
import vn.vuavuive.backend.modules.user.User;

import java.util.UUID;

public record ReviewResponse(
        UUID id,
        String userId,
        String userName,
        String userAvatar,
        UUID productId,
        Integer rating,
        String comment,
        String createdAt
) {
    @JsonProperty("_id")
    public UUID legacyId() { return id; }

    public static ReviewResponse from(Review r, User user) {
        String fullName = r.getUserName() != null ? r.getUserName() : (user != null ? user.getFullName() : "Khách hàng");
        String avatar   = user != null ? user.getAvatarUrl() : null;
        String created  = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;

        return new ReviewResponse(
                r.getId() != null ? UUID.fromString(r.getId()) : null,
                r.getUserId(),
                fullName,
                avatar,
                r.getProductId() != null ? UUID.fromString(r.getProductId()) : null,
                r.getRating(),
                r.getComment(),
                created
        );
    }

    public static ReviewResponse from(Review r) {
        return from(r, null);
    }
}
