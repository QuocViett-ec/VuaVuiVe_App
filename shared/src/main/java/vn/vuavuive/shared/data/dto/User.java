package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Locale;

public class User {
    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("provider")
    private String provider;  // "local" | "google"

    @SerializedName("address")
    private String address;

    @SerializedName("role")
    private String role;  // "user"/"customer", "admin", "staff", "audit", "shipper"

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("points")
    private Integer points;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAvatar() { return avatar; }
    public String getProvider() { return provider; }
    public String getAddress() { return address; }
    public String getRole() { return role != null ? role.toLowerCase(Locale.US) : null; }
    public boolean isActive() { return isActive; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public Integer getPoints() { return points != null ? points : 0; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setProvider(String provider) { this.provider = provider; }
    public void setAddress(String address) { this.address = address; }
    public void setRole(String role) { this.role = role != null ? role.toLowerCase() : null; }
    public void setActive(boolean active) { isActive = active; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setPoints(Integer points) { this.points = points; }

    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }
    public boolean isStaff() { return "staff".equalsIgnoreCase(role); }
    public boolean isAudit() { return "audit".equalsIgnoreCase(role); }
    public boolean isBackoffice() { return isAdmin() || isStaff() || isAudit(); }
    public boolean isShipper() { return "shipper".equalsIgnoreCase(role); }
    public boolean isGoogleProvider() { return "google".equalsIgnoreCase(provider); }
}
