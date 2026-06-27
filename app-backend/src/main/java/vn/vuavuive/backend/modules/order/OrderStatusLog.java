package vn.vuavuive.backend.modules.order;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;
import vn.vuavuive.backend.modules.order.Order.OrderStatus;

/**
 * Lớp OrderStatusLog — Nhật ký lịch sử thay đổi trạng thái đơn hàng, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusLog extends BaseEntity {

    private String orderId;
    private OrderStatus status;
    private String note;
    private String updatedById;
    private String updatedByName;
    private String updatedByRole;

    @com.google.firebase.database.Exclude
    public OrderStatus getStatus() { return status; }
    @com.google.firebase.database.Exclude
    public void setStatus(OrderStatus status) { this.status = status; }

    @com.google.firebase.database.PropertyName("status")
    public String getStatusString() { return status != null ? status.name() : null; }
    @com.google.firebase.database.PropertyName("status")
    public void setStatusString(String status) { 
        this.status = status != null ? OrderStatus.valueOf(status) : null; 
    }

    @com.google.firebase.database.PropertyName("order_id")
    public String getOrderId() { return orderId; }
    @com.google.firebase.database.PropertyName("order_id")
    public void setOrderId(String orderId) { this.orderId = orderId; }

    @com.google.firebase.database.PropertyName("updated_by_id")
    public String getUpdatedById() { return updatedById; }
    @com.google.firebase.database.PropertyName("updated_by_id")
    public void setUpdatedById(String updatedById) { this.updatedById = updatedById; }

    @com.google.firebase.database.PropertyName("updated_by_name")
    public String getUpdatedByName() { return updatedByName; }
    @com.google.firebase.database.PropertyName("updated_by_name")
    public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }

    @com.google.firebase.database.PropertyName("updated_by_role")
    public String getUpdatedByRole() { return updatedByRole; }
    @com.google.firebase.database.PropertyName("updated_by_role")
    public void setUpdatedByRole(String updatedByRole) { this.updatedByRole = updatedByRole; }
}
