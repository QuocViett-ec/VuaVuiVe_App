package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class StatusEvent {
    @SerializedName("status")
    private String status;

    @SerializedName("note")
    private String note;

    @SerializedName("timestamp")
    private String timestamp;

    public String getStatus() { return status; }
    public String getNote() { return note; }
    public String getTimestamp() { return timestamp; }
}
