package vn.vuavuive.backend.modules.review;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Lớp Review — Lưu đánh giá sản phẩm của khách hàng, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    private String userId;
    private String userName;
    private String productId;
    private Integer rating; // 1–5
    private String comment;

    @Builder.Default
    private Boolean isHidden = false;

    @com.google.firebase.database.PropertyName("user_id")
    public String getUserId() { return userId; }
    @com.google.firebase.database.PropertyName("user_id")
    public void setUserId(String userId) { this.userId = userId; }

    @com.google.firebase.database.PropertyName("user_name")
    public String getUserName() { return userName; }
    @com.google.firebase.database.PropertyName("user_name")
    public void setUserName(String userName) { this.userName = userName; }

    @com.google.firebase.database.PropertyName("product_id")
    public String getProductId() { return productId; }
    @com.google.firebase.database.PropertyName("product_id")
    public void setProductId(String productId) { this.productId = productId; }

    @com.google.firebase.database.PropertyName("is_hidden")
    public Boolean getIsHidden() { return isHidden; }
    @com.google.firebase.database.PropertyName("is_hidden")
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }
}
