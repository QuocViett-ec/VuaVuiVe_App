package vn.vuavuive.shared.data.dto;

public class CreateZaloPayPaymentRequest {
    private String orderId;
    private double amount;
    private String description;

    public CreateZaloPayPaymentRequest(String orderId, double amount, String description) {
        this.orderId = orderId;
        this.amount = amount;
        this.description = description;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
