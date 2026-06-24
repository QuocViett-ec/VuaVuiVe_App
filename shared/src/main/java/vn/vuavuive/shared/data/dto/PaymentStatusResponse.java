package vn.vuavuive.shared.data.dto;

public class PaymentStatusResponse {
    private String orderId;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private double amount;
    private String message;

    public String getOrderId() { return orderId; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getTransactionId() { return transactionId; }
    public double getAmount() { return amount; }
    public String getMessage() { return message; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setMessage(String message) { this.message = message; }
}
