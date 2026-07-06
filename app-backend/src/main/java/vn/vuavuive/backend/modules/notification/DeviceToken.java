package vn.vuavuive.backend.modules.notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceToken {
    private String id;
    private String userId;
    private String token;
    private String platform;
    private String appScope;
    private Boolean active;
    private String createdAt;
    private String lastSeenAt;

    @com.google.firebase.database.PropertyName("user_id")
    public String getUserId() { return userId; }
    @com.google.firebase.database.PropertyName("user_id")
    public void setUserId(String userId) { this.userId = userId; }

    @com.google.firebase.database.PropertyName("app_scope")
    public String getAppScope() { return appScope; }
    @com.google.firebase.database.PropertyName("app_scope")
    public void setAppScope(String appScope) { this.appScope = appScope; }

    @com.google.firebase.database.PropertyName("created_at")
    public String getCreatedAt() { return createdAt; }
    @com.google.firebase.database.PropertyName("created_at")
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @com.google.firebase.database.PropertyName("last_seen_at")
    public String getLastSeenAt() { return lastSeenAt; }
    @com.google.firebase.database.PropertyName("last_seen_at")
    public void setLastSeenAt(String lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}
