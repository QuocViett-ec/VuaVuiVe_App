package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    @SerializedName("_id")
    private String id;

    @SerializedName("orderId")
    private String orderId;  // "ORD-XXXXXXXX"

    @SerializedName("userId")
    private String userId;

    @SerializedName("shipperId")
    private String shipperId;

    @SerializedName("items")
    private List<OrderItem> items;

    @SerializedName("delivery")
    private DeliveryInfo delivery;

    @SerializedName("payment")
    private PaymentDetail payment;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("voucherId")
    private String voucherId;

    @SerializedName("voucherCode")
    private String voucherCode;

    @SerializedName("shippingFee")
    private double shippingFee;

    @SerializedName("discount")
    private double discount;

    @SerializedName("subtotal")
    private double subtotal;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("finalAmount")
    private double finalAmount;

    @SerializedName("status")
    private String status;

    @SerializedName("deliveredAt")
    private String deliveredAt;

    @SerializedName("shipmentIds")
    private List<String> shipmentIds;

    @SerializedName("returnRequest")
    private ReturnRequest returnRequest;

    @SerializedName("note")
    private String note;

    /** Flat fields từ backend OrderResponse — Shipper App sử dụng để hiển thị thông tin giao hàng */
    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("deliveryName")
    private String deliveryName;

    @SerializedName("deliveryPhone")
    private String deliveryPhone;

    @SerializedName("failReason")
    private String failReason;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("stock_restored")
    private boolean stockRestored;

    // Getters
    public boolean isStockRestored() { return stockRestored; }
    public String getId() { return id != null ? id : orderId; }
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getShipperId() { return shipperId; }
    public List<OrderItem> getItems() { return items; }
    public DeliveryInfo getDelivery() { return delivery; }
    public PaymentDetail getPayment() {
        if (payment == null && (paymentMethod != null || paymentStatus != null)) {
            payment = new PaymentDetail();
            payment.setMethod(paymentMethod == null ? null : paymentMethod.toLowerCase());
            payment.setStatus(paymentStatus == null ? null : paymentStatus.toLowerCase());
            payment.setAmount(getFinalAmount());
        }
        return payment;
    }
    public String getVoucherId() { return voucherId; }
    public String getVoucherCode() { return voucherCode; }
    public double getShippingFee() { return shippingFee; }
    public double getDiscount() { return discount; }
    public double getSubtotal() { return subtotal; }
    public double getTotalAmount() { return totalAmount; }
    public double getFinalAmount() { return finalAmount > 0 ? finalAmount : totalAmount; }
    public String getStatus() { return status; }
    public String getDeliveredAt() { return deliveredAt; }
    public List<String> getShipmentIds() { return shipmentIds; }
    public ReturnRequest getReturnRequest() { return returnRequest; }
    public String getNote() { return note; }
    public String getFailReason() { return failReason; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Lấy tên người nhận hàng.
     * Ưu tiên field phẳng deliveryName (từ backend OrderResponse mới),
     * fallback sang DeliveryInfo.getName() nếu có.
     */
    public String getRecipientName() {
        if (deliveryName != null && !deliveryName.isEmpty()) return deliveryName;
        if (delivery != null) return delivery.getName();
        String parsed = parseFromAddress(1);
        if (parsed != null) return parsed;
        return null;
    }

    /**
     * Lấy SĐT người nhận hàng.
     * Ưu tiên field phẳng deliveryPhone (từ backend OrderResponse mới),
     * fallback sang DeliveryInfo.getPhone() nếu có.
     */
    public String getRecipientPhone() {
        if (deliveryPhone != null && !deliveryPhone.isEmpty()) return deliveryPhone;
        if (delivery != null) return delivery.getPhone();
        String parsed = parseFromAddress(2);
        if (parsed != null) return parsed;
        return null;
    }

    /**
     * Lấy địa chỉ giao hàng.
     * Ưu tiên DeliveryInfo.getAddress() nếu có, fallback sang field phẳng deliveryAddress.
     */
    public String getRecipientAddress() {
        if (delivery != null && delivery.getAddress() != null) return delivery.getAddress();
        String parsed = parseFromAddress(3);
        if (parsed != null) return parsed;
        return deliveryAddress;
    }

    private String parseFromAddress(int part) {
        if (deliveryAddress == null || deliveryAddress.isEmpty()) return null;
        if (deliveryAddress.contains(" (") && deliveryAddress.contains("): ")) {
            try {
                int nameEnd = deliveryAddress.indexOf(" (");
                int phoneEnd = deliveryAddress.indexOf("): ");
                if (nameEnd > 0 && phoneEnd > nameEnd) {
                    if (part == 1) {
                        return deliveryAddress.substring(0, nameEnd).trim();
                    } else if (part == 2) {
                        return deliveryAddress.substring(nameEnd + 2, phoneEnd).trim();
                    } else if (part == 3) {
                        return deliveryAddress.substring(phoneEnd + 3).trim();
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setShipperId(String shipperId) { this.shipperId = shipperId; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void setDelivery(DeliveryInfo delivery) { this.delivery = delivery; }
    public void setPayment(PaymentDetail payment) { this.payment = payment; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }
    public void setDiscount(double discount) { this.discount = discount; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
    public void setDeliveredAt(String deliveredAt) { this.deliveredAt = deliveredAt; }
    public void setShipmentIds(List<String> shipmentIds) { this.shipmentIds = shipmentIds; }
    public void setReturnRequest(ReturnRequest returnRequest) { this.returnRequest = returnRequest; }
    public void setNote(String note) { this.note = note; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setDeliveryName(String deliveryName) { this.deliveryName = deliveryName; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setStockRestored(boolean stockRestored) { this.stockRestored = stockRestored; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        if (payment != null) payment.setStatus(paymentStatus);
    }

    // Alias setters for Firebase mapping (writes to flat delivery fields)
    public void setRecipientName(String name) { this.deliveryName = name; }
    public void setRecipientPhone(String phone) { this.deliveryPhone = phone; }
    public void setRecipientAddress(String address) { this.deliveryAddress = address; }

    // Alias: customerId == userId for shipment creation context
    public String getCustomerId() { return userId; }

    // Helper methods
    public boolean isCancellable() {
        return "pending_payment".equalsIgnoreCase(status)
                || "pending_approval".equalsIgnoreCase(status);
    }

    public boolean isReturnable() {
        return "delivered".equalsIgnoreCase(status);
    }

    public boolean isPaid() {
        return (paymentStatus != null && "paid".equalsIgnoreCase(paymentStatus))
                || (payment != null && "paid".equalsIgnoreCase(payment.getStatus()));
    }
}
