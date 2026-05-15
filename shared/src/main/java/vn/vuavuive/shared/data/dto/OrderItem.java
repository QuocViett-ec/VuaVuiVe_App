package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    @SerializedName("productId")
    private String productId;

    @SerializedName("name")
    private String name;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("price")
    private double price;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("unit")
    private String unit;

    public String getProductId() { return productId; }
    public String getName() { return name; }
    /** Alias của getName() — tương thích với OrderAdapter */
    public String getProductName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public double getLineTotal() { return price * quantity; }
}
