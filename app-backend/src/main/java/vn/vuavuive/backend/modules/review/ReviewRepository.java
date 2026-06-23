package vn.vuavuive.backend.modules.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /** Lấy danh sách đánh giá của một sản phẩm (không ẩn), mới nhất trước */
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.product.id = :productId AND r.isHidden = false ORDER BY r.createdAt DESC")
    Page<Review> findByProductId(@Param("productId") UUID productId, Pageable pageable);

    /** Kiểm tra user đã đánh giá sản phẩm này chưa */
    Optional<Review> findByUserIdAndProductId(UUID userId, UUID productId);

    /** Tính rating trung bình của sản phẩm */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product.id = :productId AND r.isHidden = false")
    Double getAverageRatingByProductId(@Param("productId") UUID productId);

    /** Đếm số lượng đánh giá */
    long countByProductIdAndIsHiddenFalse(UUID productId);
}
