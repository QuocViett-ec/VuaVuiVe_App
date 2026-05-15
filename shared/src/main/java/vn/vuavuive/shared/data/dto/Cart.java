package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Cart {
    @SerializedName("items")
    private List<CartItem> items;

    @SerializedName("savedForLater")
    private List<CartItem> savedForLater;

    @SerializedName("updatedAt")
    private String updatedAt;

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public List<CartItem> getSavedForLater() { return savedForLater; }
    public void setSavedForLater(List<CartItem> savedForLater) { this.savedForLater = savedForLater; }
    public String getUpdatedAt() { return updatedAt; }

    public double getTotalPrice() {
        if (items == null) return 0;
        double total = 0;
        for (CartItem item : items) {
            if (item.getProduct() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        int qty = 0;
        for (CartItem item : items) {
            qty += item.getQuantity();
        }
        return qty;
    }
}
