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
    private String status;  // pending, confirmed, processing, packed, shipped, delivered, cancelled, return_requested, return_approved, return_rejected

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

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Getters
    public String getId() { return id != null ? id : orderId; }
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
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
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Lấy tên người nhận hàng.
     * Ưu tiên field phẳng deliveryName (từ backend OrderResponse mới),
     * fallback sang DeliveryInfo.getName() nếu có.
     */
    public String getRecipientName() {
        if (deliveryName != null && !deliveryName.isEmpty()) return deliveryName;
        return (delivery != null) ? delivery.getName() : null;
    }

    /**
     * Lấy SĐT người nhận hàng.
     * Ưu tiên field phẳng deliveryPhone (từ backend OrderResponse mới),
     * fallback sang DeliveryInfo.getPhone() nếu có.
     */
    public String getRecipientPhone() {
        if (deliveryPhone != null && !deliveryPhone.isEmpty()) return deliveryPhone;
        return (delivery != null) ? delivery.getPhone() : null;
    }

    /**
     * Lấy địa chỉ giao hàng.
     * Ưu tiên DeliveryInfo.getAddress() nếu có, fallback sang field phẳng deliveryAddress.
     */
    public String getRecipientAddress() {
        if (delivery != null && delivery.getAddress() != null) return delivery.getAddress();
        return deliveryAddress;
    }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        if (payment != null) payment.setStatus(paymentStatus);
    }

    // Alias: customerId == userId for shipment creation context
    public String getCustomerId() { return userId; }

    // Helper methods
    public boolean isCancellable() {
        return "pending".equalsIgnoreCase(status) || "confirmed".equalsIgnoreCase(status);
    }

    public boolean isReturnable() {
        return "delivered".equalsIgnoreCase(status);
    }

    public boolean isPaid() {
        return (paymentStatus != null && "paid".equalsIgnoreCase(paymentStatus))
                || (payment != null && "paid".equalsIgnoreCase(payment.getStatus()));
    }
}
