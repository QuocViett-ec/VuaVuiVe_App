package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Shipment {
    @SerializedName("_id")
    private String id;

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("customerId")
    private String customerId;

    @SerializedName("carrier")
    private String carrier;  // GHN, GHTK, VNPost, Viettel Post, J&T, Lalamove

    @SerializedName("trackingNumber")
    private String trackingNumber;

    @SerializedName("shippingFee")
    private double shippingFee;

    @SerializedName("eta")
    private String eta;

    @SerializedName("deliveredAt")
    private String deliveredAt;

    @SerializedName("currentStatus")
    private String currentStatus;  // pending, confirmed, picked_up, in_transit, out_for_delivery, delivered, failed, returning, returned

    @SerializedName("statusHistory")
    private List<StatusEvent> statusHistory;

    @SerializedName("createdAt")
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }
    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }
    public String getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(String deliveredAt) { this.deliveredAt = deliveredAt; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public List<StatusEvent> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusEvent> statusHistory) { this.statusHistory = statusHistory; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
