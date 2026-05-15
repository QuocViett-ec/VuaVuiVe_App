package vn.vuavuive.shared.data.dto.request;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("identifier")
    private String identifier;  // email or phone

    @SerializedName("password")
    private String password;

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() { return identifier; }
    public String getPassword() { return password; }
}
