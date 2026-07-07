package vn.vuavuive.backend.modules.product;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Lớp Product — Thông tin sản phẩm, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    private String categoryId;
    private String name;
    private String description;
    
    private java.math.BigDecimal originalPrice;
    private java.math.BigDecimal sellingPrice;
    
    @Builder.Default
    private Integer stockQuantity = 0;
    
    @Builder.Default
    private String unit = "KG";
    
    private String imageUrl;

    @Builder.Default
    private java.util.List<String> images = new java.util.ArrayList<>();
    
    @Builder.Default
    private Boolean isActive = true;
    
    private String slug;
    private String subCategory;
    private Object tags;
    private String externalId;

    @com.google.firebase.database.PropertyName("category_id")
    public String getCategoryId() { return categoryId; }
    @com.google.firebase.database.PropertyName("category_id")
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    @com.google.firebase.database.PropertyName("original_price")
    public java.math.BigDecimal getOriginalPrice() { return originalPrice; }
    @com.google.firebase.database.PropertyName("original_price")
    public void setOriginalPrice(java.math.BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    @com.google.firebase.database.PropertyName("selling_price")
    public java.math.BigDecimal getSellingPrice() { return sellingPrice; }
    @com.google.firebase.database.PropertyName("selling_price")
    public void setSellingPrice(java.math.BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    @com.google.firebase.database.PropertyName("stock_quantity")
    public Integer getStockQuantity() { return stockQuantity; }
    @com.google.firebase.database.PropertyName("stock_quantity")
    public void setStockQuantity(Integer stockQuantity) { 
        if (stockQuantity != null) this.stockQuantity = stockQuantity; 
    }

    @com.google.firebase.database.PropertyName("stock")
    public Integer getStock() { return stockQuantity; }
    @com.google.firebase.database.PropertyName("stock")
    public void setStock(Integer stock) { 
        if (stock != null) this.stockQuantity = stock; 
    }

    @com.google.firebase.database.PropertyName("image_url")
    public String getImageUrl() { return imageUrl; }
    @com.google.firebase.database.PropertyName("image_url")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @com.google.firebase.database.PropertyName("images")
    public java.util.List<String> getImages() { return images; }
    @com.google.firebase.database.PropertyName("images")
    public void setImages(java.util.List<String> images) { this.images = images; }

    @com.google.firebase.database.PropertyName("is_active")
    public Boolean getIsActive() { return isActive; }
    @com.google.firebase.database.PropertyName("is_active")
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @com.google.firebase.database.PropertyName("sub_category")
    public String getSubCategory() { return subCategory; }
    @com.google.firebase.database.PropertyName("sub_category")
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    @com.google.firebase.database.PropertyName("external_id")
    public String getExternalId() { return externalId; }
    @com.google.firebase.database.PropertyName("external_id")
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
