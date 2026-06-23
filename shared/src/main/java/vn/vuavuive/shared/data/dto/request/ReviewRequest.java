package vn.vuavuive.shared.data.dto.request;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {

    @SerializedName("productId")
    private String productId;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    public ReviewRequest(String productId, int rating, String comment) {
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
