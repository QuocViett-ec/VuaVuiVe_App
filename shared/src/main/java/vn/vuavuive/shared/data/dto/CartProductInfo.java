package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class CartProductInfo {
    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private double price;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("unit")
    private String unit;

    @SerializedName("stock")
    private int stock;

    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getUnit() { return unit; }
    public int getStock() { return stock; }
}
