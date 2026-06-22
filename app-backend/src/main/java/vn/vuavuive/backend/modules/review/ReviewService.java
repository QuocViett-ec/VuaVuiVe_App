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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
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

        // Kiểm tra xem user đã đánh giá sản phẩm này chưa
        if (reviewRepository.findByUserIdAndProductId(user.getId(), product.getId()).isPresent()) {
            throw AppException.conflict("Bạn đã đánh giá sản phẩm này rồi");
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
}
