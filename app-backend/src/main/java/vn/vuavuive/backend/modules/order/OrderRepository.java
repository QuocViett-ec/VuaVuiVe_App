package vn.vuavuive.backend.modules.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(firebase.get("orders/" + id, Order.class));
    }

    public Optional<Order> findById(UUID id) {
        return findById(id.toString());
    }

    public List<Order> findAll() {
        return firebase.getList("orders", Order.class);
    }

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(UUID.randomUUID().toString());
        }
        firebase.save("orders/" + order.getId(), order);
        return order;
    }

    /**
     * Cập nhật chỉ một số field (PATCH), không ghi đè toàn bộ document.
     * Giúp Firebase Realtime listeners nhận đúng sự kiện thay đổi.
     */
    public void patch(String orderId, Map<String, Object> fields) {
        firebase.update("orders/" + orderId, fields);
    }

    public void deleteById(String id) {
        firebase.delete("orders/" + id);
    }

    private <T> Page<T> paginate(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        if (start > list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    public Page<Order> findAll(Pageable pageable) {
        List<Order> sorted = findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
        return paginate(sorted, pageable);
    }

    public Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable) {
        if (userId == null) return new PageImpl<>(List.of(), pageable, 0);
        List<Order> sorted = findAll().stream()
                .filter(o -> userId.equals(o.getUserId()))
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
        return paginate(sorted, pageable);
    }

    public Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, Order.OrderStatus status, Pageable pageable) {
        if (userId == null) return new PageImpl<>(List.of(), pageable, 0);
        List<Order> sorted = findAll().stream()
                .filter(o -> userId.equals(o.getUserId()) && status == o.getStatus())
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
        return paginate(sorted, pageable);
    }

    public Page<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status, Pageable pageable) {
        List<Order> sorted = findAll().stream()
                .filter(o -> status == o.getStatus())
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
        return paginate(sorted, pageable);
    }

    public Page<Order> findByShipperIdOrderByCreatedAtDesc(String shipperId, Pageable pageable) {
        if (shipperId == null) return new PageImpl<>(List.of(), pageable, 0);
        List<Order> sorted = findAll().stream()
                .filter(o -> shipperId.equals(o.getShipperId()))
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
        return paginate(sorted, pageable);
    }

    public Page<Order> findByShipperIdAndStatusOrderByCreatedAtDesc(String shipperId, Order.OrderStatus status, Pageable pageable) {
        if (shipperId == null) return new PageImpl<>(List.of(), pageable, 0);
        List<Order> sorted = findAll().stream()
                .filter(o -> shipperId.equals(o.getShipperId()) && status == o.getStatus())
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
        return paginate(sorted, pageable);
    }

    public List<Order> findExpiredUnpaidOrders(LocalDateTime cutoffTime) {
        if (cutoffTime == null) return List.of();
        // So sánh ISO string
        String cutoffStr = cutoffTime.toInstant(ZoneOffset.UTC).toString();
        return findAll().stream()
                .filter(o -> Order.OrderStatus.PENDING_PAYMENT == o.getStatus())
                .filter(o -> "MOMO".equalsIgnoreCase(o.getPaymentMethod()) || "ZALOPAY".equalsIgnoreCase(o.getPaymentMethod()))
                .filter(o -> Order.PaymentStatus.UNPAID == o.getPaymentStatus() || Order.PaymentStatus.PENDING == o.getPaymentStatus())
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().compareTo(cutoffStr) < 0)
                .collect(Collectors.toList());
    }
}
