package vn.vuavuive.backend.modules.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderStatusLogRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<OrderStatusLog> findById(String id) {
        return Optional.ofNullable(firebase.get("orderStatusLogs/" + id, OrderStatusLog.class));
    }

    public List<OrderStatusLog> findAll() {
        return firebase.getList("orderStatusLogs", OrderStatusLog.class);
    }

    public OrderStatusLog save(OrderStatusLog log) {
        if (log.getId() == null) {
            log.setId(UUID.randomUUID().toString());
        }
        firebase.save("orderStatusLogs/" + log.getId(), log);
        return log;
    }

    public List<OrderStatusLog> findByOrderIdOrderByCreatedAtAsc(UUID orderId) {
        if (orderId == null) return List.of();
        String orderIdStr = orderId.toString();
        return findAll().stream()
                .filter(l -> orderIdStr.equals(l.getOrderId()))
                .sorted(Comparator.comparing(OrderStatusLog::getCreatedAt, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }
}
