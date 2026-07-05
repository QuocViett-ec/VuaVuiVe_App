package vn.vuavuive.backend.modules.order;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lớp Order — Đơn hàng của khách, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    private String userId;
    private String userName;
    private String userPhone;
    
    private String shipperId;
    private String shipperName;

    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_APPROVAL;

    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal finalAmount;

    private String paymentMethod; // COD, MOMO, ZALOPAY
    
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private String deliveryAddress;
    private String deliveryName;
    private String deliveryPhone;
    
    private String note;
    
    @Builder.Default
    private Boolean pointsAdded = false;

    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("stock_restored")
    private Boolean stockRestored = false;

    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("return_request")
    private Map<String, Object> returnRequest = new java.util.HashMap<>();

    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder.Default
    private List<OrderStatusLog> statusLogs = new ArrayList<>();

    @com.google.firebase.database.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<OrderItem> getOrderItems() { return orderItems; }
    @com.google.firebase.database.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
 
    @com.google.firebase.database.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<OrderStatusLog> getStatusLogs() { return statusLogs; }
    @com.google.firebase.database.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setStatusLogs(List<OrderStatusLog> statusLogs) { this.statusLogs = statusLogs; }
 
    @com.google.firebase.database.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    public OrderStatus getStatus() { return status; }
    @com.google.firebase.database.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setStatus(OrderStatus status) { this.status = status; }
 
    @com.google.firebase.database.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    @com.google.firebase.database.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    @com.google.firebase.database.PropertyName("status")
    @com.fasterxml.jackson.annotation.JsonProperty("status")
    public String getStatusString() { return status != null ? status.name() : null; }
    @com.google.firebase.database.PropertyName("status")
    @com.fasterxml.jackson.annotation.JsonProperty("status")
    public void setStatusString(String status) { 
        this.status = status != null ? OrderStatus.valueOf(status) : null; 
    }

    @com.google.firebase.database.PropertyName("payment_status")
    @com.fasterxml.jackson.annotation.JsonProperty("payment_status")
    public String getPaymentStatusString() { return paymentStatus != null ? paymentStatus.name() : null; }
    @com.google.firebase.database.PropertyName("payment_status")
    @com.fasterxml.jackson.annotation.JsonProperty("payment_status")
    public void setPaymentStatusString(String paymentStatus) { 
        this.paymentStatus = paymentStatus != null ? PaymentStatus.valueOf(paymentStatus) : null; 
    }

    @com.google.firebase.database.PropertyName("user_id")
    @com.fasterxml.jackson.annotation.JsonProperty("user_id")
    public String getUserId() { return userId; }
    @com.google.firebase.database.PropertyName("user_id")
    @com.fasterxml.jackson.annotation.JsonProperty("user_id")
    public void setUserId(String userId) { this.userId = userId; }

    @com.google.firebase.database.PropertyName("user_name")
    @com.fasterxml.jackson.annotation.JsonProperty("user_name")
    public String getUserName() { return userName; }
    @com.google.firebase.database.PropertyName("user_name")
    @com.fasterxml.jackson.annotation.JsonProperty("user_name")
    public void setUserName(String userName) { this.userName = userName; }

    @com.google.firebase.database.PropertyName("user_phone")
    @com.fasterxml.jackson.annotation.JsonProperty("user_phone")
    public String getUserPhone() { return userPhone; }
    @com.google.firebase.database.PropertyName("user_phone")
    @com.fasterxml.jackson.annotation.JsonProperty("user_phone")
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    @com.google.firebase.database.PropertyName("shipper_id")
    @com.fasterxml.jackson.annotation.JsonProperty("shipper_id")
    public String getShipperId() { return shipperId; }
    @com.google.firebase.database.PropertyName("shipper_id")
    @com.fasterxml.jackson.annotation.JsonProperty("shipper_id")
    public void setShipperId(String shipperId) { this.shipperId = shipperId; }

    @com.google.firebase.database.PropertyName("shipper_name")
    @com.fasterxml.jackson.annotation.JsonProperty("shipper_name")
    public String getShipperName() { return shipperName; }
    @com.google.firebase.database.PropertyName("shipper_name")
    @com.fasterxml.jackson.annotation.JsonProperty("shipper_name")
    public void setShipperName(String shipperName) { this.shipperName = shipperName; }

    @com.google.firebase.database.PropertyName("total_amount")
    @com.fasterxml.jackson.annotation.JsonProperty("total_amount")
    public java.math.BigDecimal getTotalAmount() { return totalAmount; }
    @com.google.firebase.database.PropertyName("total_amount")
    @com.fasterxml.jackson.annotation.JsonProperty("total_amount")
    public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    @com.google.firebase.database.PropertyName("final_amount")
    @com.fasterxml.jackson.annotation.JsonProperty("final_amount")
    public java.math.BigDecimal getFinalAmount() { return finalAmount; }
    @com.google.firebase.database.PropertyName("final_amount")
    @com.fasterxml.jackson.annotation.JsonProperty("final_amount")
    public void setFinalAmount(java.math.BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    @com.google.firebase.database.PropertyName("payment_method")
    @com.fasterxml.jackson.annotation.JsonProperty("payment_method")
    public String getPaymentMethod() { return paymentMethod; }
    @com.google.firebase.database.PropertyName("payment_method")
    @com.fasterxml.jackson.annotation.JsonProperty("payment_method")
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    @com.google.firebase.database.PropertyName("delivery_address")
    @com.fasterxml.jackson.annotation.JsonProperty("delivery_address")
    public String getDeliveryAddress() { return deliveryAddress; }
    @com.google.firebase.database.PropertyName("delivery_address")
    @com.fasterxml.jackson.annotation.JsonProperty("delivery_address")
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    @com.google.firebase.database.PropertyName("delivery_name")
    @com.fasterxml.jackson.annotation.JsonProperty("delivery_name")
    public String getDeliveryName() { return deliveryName; }
    @com.google.firebase.database.PropertyName("delivery_name")
    @com.fasterxml.jackson.annotation.JsonProperty("delivery_name")
    public void setDeliveryName(String deliveryName) { this.deliveryName = deliveryName; }

    @com.google.firebase.database.PropertyName("delivery_phone")
    @com.fasterxml.jackson.annotation.JsonProperty("delivery_phone")
    public String getDeliveryPhone() { return deliveryPhone; }
    @com.google.firebase.database.PropertyName("delivery_phone")
    @com.fasterxml.jackson.annotation.JsonProperty("delivery_phone")
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }

    @com.google.firebase.database.PropertyName("points_added")
    @com.fasterxml.jackson.annotation.JsonProperty("points_added")
    public Boolean getPointsAdded() { return pointsAdded; }
    @com.google.firebase.database.PropertyName("points_added")
    @com.fasterxml.jackson.annotation.JsonProperty("points_added")
    public void setPointsAdded(Boolean pointsAdded) { this.pointsAdded = pointsAdded; }

    @com.google.firebase.database.PropertyName("items")
    @com.fasterxml.jackson.annotation.JsonProperty("items")
    public Map<String, OrderItem> getItemsMap() {
        if (orderItems == null) return null;
        Map<String, OrderItem> map = new java.util.HashMap<>();
        for (OrderItem item : orderItems) {
            String key = item.getId() != null ? item.getId() : (item.getProductId() != null ? item.getProductId() : UUID.randomUUID().toString());
            map.put(key, item);
        }
        return map;
    }

    @com.google.firebase.database.PropertyName("items")
    @com.fasterxml.jackson.annotation.JsonProperty("items")
    public void setItemsMap(Map<String, OrderItem> map) {
        if (map == null) {
            this.orderItems = new ArrayList<>();
        } else {
            this.orderItems = new ArrayList<>(map.values());
        }
    }

    @com.google.firebase.database.PropertyName("status_logs")
    @com.fasterxml.jackson.annotation.JsonProperty("status_logs")
    public Map<String, OrderStatusLog> getStatusLogsMap() {
        if (statusLogs == null) return null;
        Map<String, OrderStatusLog> map = new java.util.HashMap<>();
        for (OrderStatusLog log : statusLogs) {
            String key = log.getId() != null ? log.getId() : UUID.randomUUID().toString();
            map.put(key, log);
        }
        return map;
    }

    @com.google.firebase.database.PropertyName("status_logs")
    @com.fasterxml.jackson.annotation.JsonProperty("status_logs")
    public void setStatusLogsMap(Map<String, OrderStatusLog> map) {
        if (map == null) {
            this.statusLogs = new ArrayList<>();
        } else {
            this.statusLogs = new ArrayList<>(map.values());
        }
    }

    public enum OrderStatus {
        PENDING_PAYMENT,
        PENDING_APPROVAL,
        CONFIRMED,
        IN_TRANSIT,
        DELIVERED,
        FAILED,
        RETURNED,
        CANCELLED
    }

    public enum PaymentStatus {
        UNPAID, PENDING, PAID, FAILED, CANCELLED, REFUNDED
    }
}
