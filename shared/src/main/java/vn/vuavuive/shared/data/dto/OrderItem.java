package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    @SerializedName("productId")
    private String productId;

    @SerializedName(value = "name", alternate = {"productName"})
    private String name;

    @SerializedName(value = "imageUrl", alternate = {"productImageUrl"})
    private String imageUrl;

    @SerializedName(value = "price", alternate = {"unitPrice"})
    private double price;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("unit")
    private String unit;

    @SerializedName("subtotal")
    private Double subtotal;

    public String getProductId() { return productId; }
    public String getName() { return name; }
    /** Alias của getName() — tương thích với OrderAdapter */
    public String getProductName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public double getLineTotal() { return subtotal != null ? subtotal : price * quantity; }

    // Setters
    public void setProductId(String productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
