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
}
