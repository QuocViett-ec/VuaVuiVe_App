package vn.vuavuive.backend.modules.review;

import jakarta.persistence.*;
import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;
import vn.vuavuive.backend.modules.product.Product;
import vn.vuavuive.backend.modules.user.User;

/**
 * Bảng REVIEWS — Lưu đánh giá sản phẩm của khách hàng.
 * Một User có thể đánh giá nhiều sản phẩm, mỗi sản phẩm chỉ được đánh giá một lần bởi một user.
 */
@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1–5

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_hidden", nullable = false)
    @Builder.Default
    private Boolean isHidden = false;
}
