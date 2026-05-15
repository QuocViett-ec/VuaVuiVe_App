package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("productId")
    private String productId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("product")
    private CartProductInfo product;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public CartProductInfo getProduct() { return product; }
    public void setProduct(CartProductInfo product) { this.product = product; }
}
