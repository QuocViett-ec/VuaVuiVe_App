package vn.vuavuive.backend.modules.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.product.Product;
import vn.vuavuive.backend.modules.product.ProductRepository;
import vn.vuavuive.backend.modules.review.dto.ReviewRequest;
import vn.vuavuive.backend.modules.review.dto.ReviewResponse;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;
import vn.vuavuive.backend.security.JwtUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.order.OrderRepository;
import vn.vuavuive.backend.modules.order.OrderItem;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final JwtUtils jwtUtils;

    /** Lấy danh sách đánh giá của sản phẩm (public) */
    public ApiResponse<List<ReviewResponse>> getProductReviews(UUID productId, int page, int size) {
        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            throw AppException.notFound("Sản phẩm");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);

        List<ReviewResponse> data = reviews.getContent()
                .stream()
                .map(ReviewResponse::from)
                .toList();

        return ApiResponse.<List<ReviewResponse>>builder()
                .success(true)
                .message("Thao tác thành công")
                .data(data)
                .build();
    }

    /** Gửi đánh giá sản phẩm (yêu cầu đăng nhập) */
    @Transactional
    public ApiResponse<ReviewResponse> submitReview(String authHeader, ReviewRequest request) {
        // Lấy user từ JWT token
        String token = extractToken(authHeader);
        String identifier = jwtUtils.getEmailFromToken(token);

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> AppException.notFound("Người dùng"));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));

        // Kiểm tra xem user đã đánh giá sản phẩm này chưa, nếu rồi thì cập nhật
        java.util.Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(user.getId(), product.getId());
        if (existingReview.isPresent()) {
            Review review = existingReview.get();
            review.setRating(request.rating());
            review.setComment(request.comment());
            Review saved = reviewRepository.save(review);
            return ApiResponse.success(ReviewResponse.from(saved));
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        Review saved = reviewRepository.save(review);
        return ApiResponse.success(ReviewResponse.from(saved));
    }

    /** Cập nhật đánh giá (chủ đánh giá hoặc admin) */
    @Transactional
    public ApiResponse<ReviewResponse> updateReview(String authHeader, UUID reviewId, ReviewRequest request) {
        String token = extractToken(authHeader);
        String identifier = jwtUtils.getEmailFromToken(token);

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> AppException.notFound("Người dùng"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> AppException.notFound("Đánh giá"));

        // Chỉ chủ đánh giá mới được sửa (hoặc admin)
        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw AppException.forbidden("Bạn không có quyền sửa đánh giá này");
        }

        review.setRating(request.rating());
        review.setComment(request.comment());
        return ApiResponse.success(ReviewResponse.from(reviewRepository.save(review)));
    }

    /** Xoá đánh giá (chủ hoặc admin) */
    @Transactional
    public void deleteReview(String authHeader, UUID reviewId) {
        String token = extractToken(authHeader);
        String identifier = jwtUtils.getEmailFromToken(token);

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> AppException.notFound("Người dùng"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> AppException.notFound("Đánh giá"));

        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw AppException.forbidden("Bạn không có quyền xoá đánh giá này");
        }

        reviewRepository.delete(review);
    }

    /** Lấy rating trung bình của sản phẩm */
    public Double getAverageRating(UUID productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để thực hiện thao tác này");
        }
        return authHeader.substring(7);
    }

    /** Gửi đánh giá cho các sản phẩm trong đơn hàng */
    @Transactional
    public ApiResponse<ReviewResponse> submitOrderReviews(String authHeader, UUID orderId, java.util.Map<String, Object> body) {
        List<Map<String, Object>> reviewsPayload = (List<Map<String, Object>>) body.get("reviews");
        if (reviewsPayload == null || reviewsPayload.isEmpty()) {
            throw AppException.badRequest("Danh sách đánh giá trống");
        }

        String token = extractToken(authHeader);
        String identifier = jwtUtils.getEmailFromToken(token);

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> AppException.notFound("Người dùng"));

        ReviewResponse lastResponse = null;
        for (Map<String, Object> payload : reviewsPayload) {
            String prodIdStr = (String) payload.get("productId");
            if (prodIdStr == null) continue;
            UUID productId = UUID.fromString(prodIdStr);
            Number ratingNum = (Number) payload.get("rating");
            int rating = ratingNum != null ? ratingNum.intValue() : 0;
            String comment = (String) payload.get("comment");

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> AppException.notFound("Sản phẩm"));

            // Upsert review
            java.util.Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(user.getId(), product.getId());
            Review review;
            if (existingReview.isPresent()) {
                review = existingReview.get();
                review.setRating(rating);
                review.setComment(comment);
            } else {
                review = Review.builder()
                        .user(user)
                        .product(product)
                        .rating(rating)
                        .comment(comment)
                        .build();
            }
            Review saved = reviewRepository.save(review);
            lastResponse = ReviewResponse.from(saved);
        }

        return ApiResponse.success(lastResponse);
    }

    /** Lấy nhận xét của người dùng cho đơn hàng này */
    public ApiResponse<ReviewResponse> getMyReviewForOrder(String authHeader, UUID orderId) {
        String token = extractToken(authHeader);
        String identifier = jwtUtils.getEmailFromToken(token);

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> AppException.notFound("Người dùng"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        for (OrderItem item : order.getOrderItems()) {
            java.util.Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(user.getId(), item.getProduct().getId());
            if (existingReview.isPresent()) {
                return ApiResponse.success(ReviewResponse.from(existingReview.get()));
            }
        }

        return ApiResponse.<ReviewResponse>builder()
                .success(true)
                .message("Chưa có đánh giá")
                .data(null)
                .build();
    }
}
