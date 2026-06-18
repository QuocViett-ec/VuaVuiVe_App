package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class PaymentDetail {
    @SerializedName("method")
    private String method;  // "cod", "vnpay", "momo"

    @SerializedName("status")
    private String status;  // "pending", "paid", "refunded"

    @SerializedName("gateway")
    private String gateway;

    @SerializedName("transactionId")
    private String transactionId;

    @SerializedName("transactionTime")
    private String transactionTime;

    @SerializedName("amount")
    private double amount;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGateway() { return gateway; }
    public String getTransactionId() { return transactionId; }
    public String getTransactionTime() { return transactionTime; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isPaid() { return "paid".equals(status); }
    public boolean isCOD() { return "cod".equals(method); }
    public boolean isVNPay() { return "vnpay".equals(method); }
    public boolean isMoMo() { return "momo".equals(method); }
}
