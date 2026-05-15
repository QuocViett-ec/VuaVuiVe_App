package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class ReturnRequest {
    @SerializedName("reason")
    private String reason;

    @SerializedName("status")
    private String status;  // "pending", "approved", "rejected"

    @SerializedName("adminNote")
    private String adminNote;

    @SerializedName("requestedAt")
    private String requestedAt;

    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getAdminNote() { return adminNote; }
    public String getRequestedAt() { return requestedAt; }
}
