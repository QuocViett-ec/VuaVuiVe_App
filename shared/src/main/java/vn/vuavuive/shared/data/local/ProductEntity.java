package vn.vuavuive.shared.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import vn.vuavuive.shared.data.dto.Product;

/**
 * ProductEntity — Offline cache cho sản phẩm.
 * Hiển thị từ Room khi mất kết nối mạng.
 */
@Entity(tableName = "products")
public class ProductEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private String slug;
    private double price;
    private Double originalPrice;
    private String category;
    private String subCategory;
    private String description;
    private String imageUrl;
    private int stock;
    private String unit;
    private boolean isActive;
    private Double rating;
    private Integer reviewCount;
    private Integer soldCount;
    private String createdAt;

    /** Timestamp khi cache được lưu — dùng để invalidate cache cũ */
    private long cachedAt;

    public ProductEntity() {}

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public Integer getSoldCount() { return soldCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }

    /** Cache còn hiệu lực không? (15 phút) */
    public boolean isCacheValid() {
        return System.currentTimeMillis() - cachedAt < 15 * 60 * 1000L;
    }

    // ── Conversion helpers ─────────────────────────────────────────────────
    public static ProductEntity fromProduct(Product p) {
        ProductEntity e = new ProductEntity();
        e.setId(p.getId() != null ? p.getId() : "");
        e.setName(p.getName());
        e.setSlug(p.getSlug());
        e.setPrice(p.getPrice());
        e.setOriginalPrice(p.getOriginalPrice());
        e.setCategory(p.getCategory());
        e.setSubCategory(p.getSubCategory());
        e.setDescription(p.getDescription());
        e.setImageUrl(p.getImageUrl());
        e.setStock(p.getStock());
        e.setUnit(p.getUnit());
        e.setActive(p.isActive());
        e.setRating(p.getRating());
        e.setReviewCount(p.getReviewCount());
        e.setSoldCount(p.getSoldCount());
        e.setCachedAt(System.currentTimeMillis());
        return e;
    }

    public Product toProduct() {
        Product p = new Product();
        p.setId(this.id);
        p.setName(this.name);
        p.setSlug(this.slug);
        p.setPrice(this.price);
        p.setOriginalPrice(this.originalPrice);
        p.setCategory(this.category);
        p.setSubCategory(this.subCategory);
        p.setDescription(this.description);
        p.setImageUrl(this.imageUrl);
        p.setStock(this.stock);
        p.setUnit(this.unit);
        p.setActive(this.isActive);
        p.setRating(this.rating);
        p.setReviewCount(this.reviewCount);
        p.setSoldCount(this.soldCount);
        return p;
    }
}
