package vn.vuavuive.backend.modules.shipper;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Lớp Shipper — Lưu thông tin tài xế giao hàng, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipper extends BaseEntity {

    private String fullName;
    private String phone;
    private String vehicleNumber;
    private String userId;

    @Builder.Default
    private Status currentStatus = Status.OFFLINE;

    @Builder.Default
    private Boolean isActive = true;

    @com.google.firebase.database.Exclude
    public Status getCurrentStatus() { return currentStatus; }
    @com.google.firebase.database.Exclude
    public void setCurrentStatus(Status currentStatus) { this.currentStatus = currentStatus; }

    @com.google.firebase.database.PropertyName("current_status")
    public String getCurrentStatusString() { return currentStatus != null ? currentStatus.name() : null; }
    @com.google.firebase.database.PropertyName("current_status")
    public void setCurrentStatusString(String status) { 
        this.currentStatus = status != null ? Status.valueOf(status) : null; 
    }

    @com.google.firebase.database.PropertyName("full_name")
    public String getFullName() { return fullName; }
    @com.google.firebase.database.PropertyName("full_name")
    public void setFullName(String fullName) { this.fullName = fullName; }

    @com.google.firebase.database.PropertyName("vehicle_number")
    public String getVehicleNumber() { return vehicleNumber; }
    @com.google.firebase.database.PropertyName("vehicle_number")
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    @com.google.firebase.database.PropertyName("user_id")
    public String getUserId() { return userId; }
    @com.google.firebase.database.PropertyName("user_id")
    public void setUserId(String userId) { this.userId = userId; }

    @com.google.firebase.database.PropertyName("is_active")
    public Boolean getIsActive() { return isActive; }
    @com.google.firebase.database.PropertyName("is_active")
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public enum Status {
        AVAILABLE, DELIVERING, OFFLINE
    }
}
