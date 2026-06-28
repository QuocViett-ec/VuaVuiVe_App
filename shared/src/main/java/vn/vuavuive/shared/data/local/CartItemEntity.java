package vn.vuavuive.shared.data.local;

import vn.vuavuive.shared.data.dto.CartItem;

/**
 * CartItemEntity — In-memory model for cart items.
 * Được đồng bộ trực tiếp với Firebase Realtime Database.
 * (Đã xóa Room/SQLite annotations — chỉ dùng Firebase)
 */
public class CartItemEntity {

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

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

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
