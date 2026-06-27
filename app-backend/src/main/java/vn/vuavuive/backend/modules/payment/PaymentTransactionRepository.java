package vn.vuavuive.backend.modules.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentTransactionRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<PaymentTransaction> findById(String id) {
        return Optional.ofNullable(firebase.get("paymentTransactions/" + id, PaymentTransaction.class));
    }

    public List<PaymentTransaction> findAll() {
        return firebase.getList("paymentTransactions", PaymentTransaction.class);
    }

    public PaymentTransaction save(PaymentTransaction transaction) {
        if (transaction.getId() == null) {
            transaction.setId(UUID.randomUUID().toString());
        }
        firebase.save("paymentTransactions/" + transaction.getId(), transaction);
        return transaction;
    }

    public Optional<PaymentTransaction> findByRequestId(String requestId) {
        if (requestId == null) return Optional.empty();
        return findAll().stream()
                .filter(t -> requestId.equals(t.getRequestId()))
                .findFirst();
    }

    public Optional<PaymentTransaction> findFirstByOrderAndProviderOrderByCreatedAtDesc(vn.vuavuive.backend.modules.order.Order order, String provider) {
        if (order == null || provider == null) return Optional.empty();
        String orderIdStr = order.getId();
        return findAll().stream()
                .filter(t -> orderIdStr.equals(t.getOrderId()) && provider.equals(t.getProvider()))
                .sorted(Comparator.comparing(PaymentTransaction::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .findFirst();
    }
}
