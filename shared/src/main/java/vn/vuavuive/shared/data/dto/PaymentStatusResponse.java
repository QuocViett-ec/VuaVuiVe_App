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
}
