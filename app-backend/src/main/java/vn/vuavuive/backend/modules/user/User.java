package vn.vuavuive.backend.modules.user;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Lớp User — Lưu thông tin người dùng (Khách hàng, Admin, Staff), loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    private String email;
    private String phone;
    private String passwordHash;
    private String fullName;
    
    @Builder.Default
    private Role role = Role.CUSTOMER;
    
    private String avatarUrl;
    private String address;
    
    @Builder.Default
    private Integer points = 0;
    
    @Builder.Default
    private Boolean isActive = true;

    @com.google.firebase.database.Exclude
    public Role getRole() { return role; }
    @com.google.firebase.database.Exclude
    public void setRole(Role role) { this.role = role; }

    @com.google.firebase.database.PropertyName("role")
    public String getRoleString() { return role != null ? role.name() : null; }
    @com.google.firebase.database.PropertyName("role")
    public void setRoleString(String role) { 
        this.role = role != null ? Role.valueOf(role) : null; 
    }

    @com.google.firebase.database.PropertyName("password_hash")
    public String getPasswordHash() { return passwordHash; }
    @com.google.firebase.database.PropertyName("password_hash")
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @com.google.firebase.database.PropertyName("full_name")
    public String getFullName() { return fullName; }
    @com.google.firebase.database.PropertyName("full_name")
    public void setFullName(String fullName) { this.fullName = fullName; }

    @com.google.firebase.database.PropertyName("avatar_url")
    public String getAvatarUrl() { return avatarUrl; }
    @com.google.firebase.database.PropertyName("avatar_url")
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    @com.google.firebase.database.PropertyName("is_active")
    public Boolean getIsActive() { return isActive; }
    @com.google.firebase.database.PropertyName("is_active")
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    /**
     * Phân quyền trong hệ thống
     */
    public enum Role {
        CUSTOMER, STAFF, ADMIN, SHIPPER, AUDIT
    }
}
