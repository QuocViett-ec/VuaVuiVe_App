package vn.vuavuive.shared.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import vn.vuavuive.shared.data.dto.CartItem;

/**
 * CartItemEntity — Offline-first cart storage.
 * Được đồng bộ lên server sau khi có kết nối mạng.
 */
@Entity(tableName = "cart_items")
public class CartItemEntity {

    @PrimaryKey
    @NonNull
    private String productId;

    private int quantity;

    private String productName;
    private double productPrice;
    private String productImageUrl;
    private String productUnit;
    private int productStock;

    /** Milliseconds since epoch — dùng để sort theo thời gian thêm vào */
    private long addedAt;

    /** true = đang ở "Lưu để mua sau" */
    private boolean savedForLater;

    public CartItemEntity() {}

    @NonNull
    public String getProductId() { return productId; }
    public void setProductId(@NonNull String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public String getProductUnit() { return productUnit; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }

    public int getProductStock() { return productStock; }
    public void setProductStock(int productStock) { this.productStock = productStock; }

    public long getAddedAt() { return addedAt; }
    public void setAddedAt(long addedAt) { this.addedAt = addedAt; }

    public boolean isSavedForLater() { return savedForLater; }
    public void setSavedForLater(boolean savedForLater) { this.savedForLater = savedForLater; }

    public double getLineTotal() { return productPrice * quantity; }

    // ── Conversion helper ─────────────────────────────────────────────────
    public static CartItemEntity fromCartItem(CartItem item, boolean savedForLater) {
        CartItemEntity e = new CartItemEntity();
        e.setProductId(item.getProductId());
        e.setQuantity(item.getQuantity());
        e.setSavedForLater(savedForLater);
        e.setAddedAt(System.currentTimeMillis());
        if (item.getProduct() != null) {
            e.setProductName(item.getProduct().getName());
            e.setProductPrice(item.getProduct().getPrice());
            e.setProductImageUrl(item.getProduct().getImageUrl());
            e.setProductUnit(item.getProduct().getUnit());
            e.setProductStock(item.getProduct().getStock());
        }
        return e;
    }
}
