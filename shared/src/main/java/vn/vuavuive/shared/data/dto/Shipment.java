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
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCarrier() { return carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public double getShippingFee() { return shippingFee; }
    public String getEta() { return eta; }
    public String getDeliveredAt() { return deliveredAt; }
    public String getCurrentStatus() { return currentStatus; }
    public List<StatusEvent> getStatusHistory() { return statusHistory; }
    public String getCreatedAt() { return createdAt; }
}
