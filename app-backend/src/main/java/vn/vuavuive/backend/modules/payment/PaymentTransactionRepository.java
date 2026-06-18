package vn.vuavuive.backend.modules.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByRequestId(String requestId);
    Optional<PaymentTransaction> findFirstByOrderAndProviderOrderByCreatedAtDesc(vn.vuavuive.backend.modules.order.Order order, String provider);
}
