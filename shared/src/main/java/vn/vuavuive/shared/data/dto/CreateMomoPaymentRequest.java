package vn.vuavuive.shared.data.dto;

public class CreateMomoPaymentRequest {
    private String orderId;
    private double amount;
    private String orderInfo;
    private String userId;

    public CreateMomoPaymentRequest(String orderId, double amount, String orderInfo, String userId) {
        this.orderId = orderId;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.userId = userId;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
