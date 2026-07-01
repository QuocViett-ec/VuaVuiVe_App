package vn.vuavuive.backend.modules.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.vuavuive.backend.modules.product.Product;
import vn.vuavuive.backend.modules.product.ProductRepository;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderScheduler - Tác vụ chạy nền (Cron Job) tự động hủy đơn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderStatusLogRepository statusLogRepository;

    /**
     * Tự động quét và hủy đơn hàng thanh toán online (MOMO/ZALOPAY)
     * mà khách hàng không thực hiện thanh toán sau 15 phút.
     * Chạy mỗi 1 phút.
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredUnpaidOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(15);
        List<Order> expiredOrders = orderRepository.findExpiredUnpaidOrders(cutoffTime);

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("Quét phát hiện {} đơn hàng chưa thanh toán quá 15 phút. Bắt đầu tự động hủy...", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                // 1. Hoàn lại tồn kho cho sản phẩm
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getProductId() != null) {
                        Product product = productRepository.findById(item.getProductId()).orElse(null);
                        if (product != null) {
                            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                            productRepository.save(product);
                            log.info("Hoàn trả tồn kho cho SP: {} (+{})", product.getName(), item.getQuantity());
                        }
                    }
                }

                // 2. Cập nhật trạng thái đơn hàng sang CANCELLED
                order.setStatus(Order.OrderStatus.CANCELLED);
                order.setPaymentStatus(Order.PaymentStatus.CANCELLED);
                orderRepository.save(order);

                // 3. Ghi log trạng thái đơn hàng
                OrderStatusLog statusLog = OrderStatusLog.builder()
                        .orderId(order.getId())
                        .status(Order.OrderStatus.CANCELLED)
                        .note("Tự động hủy đơn hàng do quá 15 phút chưa hoàn tất thanh toán.")
                        .updatedByRole("SYSTEM")
                        .updatedByName("AutoCancelJob")
                        .build();
                statusLogRepository.save(statusLog);

                log.info("Hủy đơn hàng quá hạn thành công: {}", order.getId());
            } catch (Exception e) {
                log.error("Lỗi khi tự động hủy đơn hàng: " + order.getId(), e);
            }
        }
    }
}
