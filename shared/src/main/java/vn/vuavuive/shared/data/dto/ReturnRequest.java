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

    // Setters
    public void setReason(String reason) { this.reason = reason; }
    public void setStatus(String status) { this.status = status; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }
}
