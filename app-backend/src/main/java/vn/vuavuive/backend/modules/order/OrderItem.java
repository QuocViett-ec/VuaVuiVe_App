package vn.vuavuive.backend.modules.order;

import jakarta.persistence.*;
import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;
import vn.vuavuive.backend.modules.product.Product;

import java.math.BigDecimal;

/**
 * Bảng ORDER_ITEMS — Chi tiết từng sản phẩm trong đơn hàng.
 * Lưu unit_price tại thời điểm đặt hàng để tránh bị ảnh hưởng
 * khi Admin thay đổi giá sản phẩm sau này.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** Giá tại thời điểm đặt hàng (snapshot) — Không thay đổi dù giá SP sau này biến động */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** = quantity * unit_price */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
