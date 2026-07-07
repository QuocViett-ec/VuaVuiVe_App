package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Product {
    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("slug")
    private String slug;

    @SerializedName("price")
    private double price;

    @SerializedName("originalPrice")
    private Double originalPrice;

    @SerializedName("category")
    private String category;

    @SerializedName("subCategory")
    private String subCategory;

    @SerializedName("description")
    private String description;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("images")
    private List<String> images;

    @SerializedName("stock")
    private int stock;

    @SerializedName("unit")
    private String unit;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName(value = "isActive", alternate = {"is_active", "active"})
    private boolean isActive;

    @SerializedName("externalId")
    private Integer externalId;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("reviewCount")
    private Integer reviewCount;

    @SerializedName("soldCount")
    private Integer soldCount;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public double getPrice() { return price; }
    public Double getOriginalPrice() { return originalPrice; }
    public String getCategory() { return category; }
    public String getSubCategory() { return subCategory; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getImages() { return images; }
    public int getStock() { return stock; }
    public String getUnit() { return unit; }
    public List<String> getTags() { return tags; }
    public boolean isActive() { return isActive; }
    public Integer getExternalId() { return externalId; }
    public Double getRating() { return rating; }
    public Integer getReviewCount() { return reviewCount; }
    public Integer getSoldCount() { return soldCount; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setPrice(double price) { this.price = price; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }
    public void setCategory(String category) { this.category = category; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setImages(List<String> images) { this.images = images; }
    public void setStock(int stock) { this.stock = stock; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setActive(boolean active) { isActive = active; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isOnSale() { return originalPrice != null && originalPrice > price; }
    public int getDiscountPercent() {
        if (!isOnSale()) return 0;
        return (int) Math.round((1 - price / originalPrice) * 100);
    }
    public boolean isInStock() { return stock > 0; }
}
