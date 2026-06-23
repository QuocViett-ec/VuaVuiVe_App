package vn.vuavuive.backend.modules.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import vn.vuavuive.backend.modules.review.Review;

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

    public static ReviewResponse from(Review r) {
        String fullName = r.getUser().getFullName();
        String avatar   = r.getUser().getAvatarUrl();
        String created  = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;

        return new ReviewResponse(
                r.getId(),
                r.getUser().getId().toString(),
                fullName,
                avatar,
                r.getProduct().getId(),
                r.getRating(),
                r.getComment(),
                created
        );
    }
}
