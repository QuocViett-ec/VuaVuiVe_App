package vn.vuavuive.shared.data.dto;

public class CreateMomoPaymentResponse {
    private String orderId;
    private String requestId;
    private double amount;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private Integer resultCode;
    private String message;

    public String getOrderId() { return orderId; }
    public String getRequestId() { return requestId; }
    public double getAmount() { return amount; }
    public String getPayUrl() { return payUrl; }
    public String getDeeplink() { return deeplink; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public Integer getResultCode() { return resultCode; }
    public String getMessage() { return message; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    public void setDeeplink(String deeplink) { this.deeplink = deeplink; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public void setResultCode(Integer resultCode) { this.resultCode = resultCode; }
    public void setMessage(String message) { this.message = message; }
}
