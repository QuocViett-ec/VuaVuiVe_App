package vn.vuavuive.backend.modules.order;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Lớp OrderItem — Chi tiết từng sản phẩm trong đơn hàng, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    private String productId;
    private String productName;
    private Integer quantity;
    private java.math.BigDecimal unitPrice;
    private java.math.BigDecimal subtotal;

    @com.google.firebase.database.PropertyName("product_id")
    public String getProductId() { return productId; }
    @com.google.firebase.database.PropertyName("product_id")
    public void setProductId(String productId) { this.productId = productId; }

    @com.google.firebase.database.PropertyName("product_name")
    public String getProductName() { return productName; }
    @com.google.firebase.database.PropertyName("product_name")
    public void setProductName(String productName) { this.productName = productName; }

    @com.google.firebase.database.PropertyName("unit_price")
    public java.math.BigDecimal getUnitPrice() { return unitPrice; }
    @com.google.firebase.database.PropertyName("unit_price")
    public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
