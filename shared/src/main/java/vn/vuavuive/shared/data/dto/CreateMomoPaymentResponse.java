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
}
