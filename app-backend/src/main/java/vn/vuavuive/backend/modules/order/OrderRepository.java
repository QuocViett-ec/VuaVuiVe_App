package vn.vuavuive.backend.modules.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /** Lấy đơn hàng của một user (App Customer xem lịch sử đơn) */
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /** Lấy đơn theo trạng thái (Admin lọc đơn PENDING/IN_TRANSIT...) */
    Page<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status, Pageable pageable);

    /** Lấy đơn được giao bởi một Shipper cụ thể */
    Page<Order> findByShipperIdOrderByCreatedAtDesc(UUID shipperId, Pageable pageable);

    /** Lấy đơn được giao bởi Shipper cụ thể, lọc theo trạng thái */
    Page<Order> findByShipperIdAndStatusOrderByCreatedAtDesc(UUID shipperId, Order.OrderStatus status, Pageable pageable);

    /**
     * Tìm các đơn VNPAY/MOMO đã PENDING quá lâu để tự động hủy (Cron Job).
     * Đây là query phục vụ tính năng auto-cancel đơn chưa thanh toán sau 15 phút.
     */
    @Query("""
        SELECT o FROM Order o
        WHERE o.status = 'PENDING'
        AND o.paymentMethod IN ('VNPAY', 'MOMO')
        AND o.paymentStatus = 'UNPAID'
        AND o.createdAt < :cutoffTime
    """)
    List<Order> findExpiredUnpaidOrders(@Param("cutoffTime") LocalDateTime cutoffTime);
}
