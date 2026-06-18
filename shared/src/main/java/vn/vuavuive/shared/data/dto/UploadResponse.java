package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    @SerializedName("url")
    private String url;

    public String getUrl() {
        return url;
    }
}
