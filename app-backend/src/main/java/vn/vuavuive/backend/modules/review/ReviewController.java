package vn.vuavuive.backend.modules.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.modules.review.dto.ReviewRequest;
import vn.vuavuive.backend.modules.review.dto.ReviewResponse;

import java.util.List;
import java.util.UUID;

@Tag(name = "Reviews", description = "Product review APIs")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get list of reviews for a product")
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, page, size));
    }

    @Operation(summary = "Submit a product review (Logged-in required)")
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ReviewRequest request
    ) {
        ApiResponse<ReviewResponse> response = reviewService.submitReview(authHeader, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Submit reviews for an order")
    @PostMapping("/orders/{orderId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitOrderReviews(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID orderId,
            @RequestBody java.util.Map<String, Object> body
    ) {
        ApiResponse<ReviewResponse> response = reviewService.submitOrderReviews(authHeader, orderId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get user review for an order")
    @GetMapping("/orders/{orderId}/reviews/me")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReviewForOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID orderId
    ) {
        ApiResponse<ReviewResponse> response = reviewService.getMyReviewForOrder(authHeader, orderId);
        return ResponseEntity.ok(response);
    }
}
