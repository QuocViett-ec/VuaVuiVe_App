package vn.vuavuive.backend.core;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class cho tất cả Model trong hệ thống, loại bỏ hoàn toàn JPA.
 */
@Getter
@Setter
public abstract class BaseEntity {

    private String id;
    private String createdAt;
    private String updatedAt;

    @com.google.firebase.database.PropertyName("created_at")
    public String getCreatedAt() { return createdAt; }
    @com.google.firebase.database.PropertyName("created_at")
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @com.google.firebase.database.PropertyName("updated_at")
    public String getUpdatedAt() { return updatedAt; }
    @com.google.firebase.database.PropertyName("updated_at")
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public BaseEntity() {
        // Tự sinh ID và mốc thời gian khi tạo mới đối tượng
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
        this.updatedAt = Instant.now().toString();
    }
}
