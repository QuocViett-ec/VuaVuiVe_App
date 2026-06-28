package vn.vuavuive.shared.data.dto;

public class CreateZaloPayPaymentResponse {
    private String orderId;
    private String appTransId;
    private double amount;
    private String orderUrl;
    private String zpTransToken;
    private String qrCodeUrl;
    private Integer returnCode;
    private String returnMessage;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getAppTransId() { return appTransId; }
    public void setAppTransId(String appTransId) { this.appTransId = appTransId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getOrderUrl() { return orderUrl; }
    public void setOrderUrl(String orderUrl) { this.orderUrl = orderUrl; }
    public String getZpTransToken() { return zpTransToken; }
    public void setZpTransToken(String zpTransToken) { this.zpTransToken = zpTransToken; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public Integer getReturnCode() { return returnCode; }
    public void setReturnCode(Integer returnCode) { this.returnCode = returnCode; }
    public String getReturnMessage() { return returnMessage; }
    public void setReturnMessage(String returnMessage) { this.returnMessage = returnMessage; }
}
