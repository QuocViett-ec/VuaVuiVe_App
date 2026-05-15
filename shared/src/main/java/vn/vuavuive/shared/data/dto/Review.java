package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("_id")
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("orderCode")
    private String orderCode;

    @SerializedName("productId")
    private String productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("productImage")
    private String productImage;

    @SerializedName("rating")
    private int rating;  // 1-5

    @SerializedName("comment")
    private String comment;  // max 500

    @SerializedName("createdAt")
    private String createdAt;

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getOrderId() { return orderId; }
    public String getOrderCode() { return orderCode; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductImage() { return productImage; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getCreatedAt() { return createdAt; }
}
