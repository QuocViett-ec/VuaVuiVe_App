package vn.vuavuive.backend.modules.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.order.OrderRepository;
import vn.vuavuive.backend.modules.order.OrderStatusLogRepository;
import vn.vuavuive.backend.modules.product.ProductRepository;
import vn.vuavuive.backend.modules.user.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayStatusTest {

    @Mock RestTemplate restTemplate;
    @Mock ObjectMapper objectMapper;
    @Mock OrderRepository orderRepository;
    @Mock PaymentTransactionRepository transactionRepository;
    @Mock OrderStatusLogRepository statusLogRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock Environment environment;

    @InjectMocks MoMoService moMoService;
    @InjectMocks ZaloPayService zaloPayService;

    @Test
    void momoSuccessReturnsToAdminApproval() {
        Order order = onlineOrder("order-1", "MOMO");
        PaymentTransaction tx = tx(order, "MOMO", "req-1");

        when(environment.matchesProfiles("dev")).thenReturn(true);
        when(transactionRepository.findByRequestId("req-1")).thenReturn(Optional.of(tx));
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        moMoService.handleMockResult("order-1", "req-1", true);

        assertEquals(PaymentTransaction.Status.PAID, tx.getStatus());
        assertEquals(Order.PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals(Order.OrderStatus.PENDING_APPROVAL, order.getStatus());
    }

    @Test
    void zaloPaySuccessReturnsToAdminApproval() {
        Order order = onlineOrder("order-2", "ZALOPAY");
        PaymentTransaction tx = tx(order, "ZALOPAY", "req-2");

        when(environment.matchesProfiles("dev")).thenReturn(true);
        when(orderRepository.findById("order-2")).thenReturn(Optional.of(order));
        when(transactionRepository.findFirstByOrderAndProviderOrderByCreatedAtDesc(order, "ZALOPAY"))
                .thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        zaloPayService.handleMockResult("order-2", true);

        assertEquals(PaymentTransaction.Status.PAID, tx.getStatus());
        assertEquals(Order.PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals(Order.OrderStatus.PENDING_APPROVAL, order.getStatus());
    }

    private static Order onlineOrder(String id, String provider) {
        Order order = Order.builder()
                .paymentMethod(provider)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .finalAmount(BigDecimal.valueOf(20_000))
                .build();
        order.setId(id);
        return order;
    }

    private static PaymentTransaction tx(Order order, String provider, String requestId) {
        return PaymentTransaction.builder()
                .orderId(order.getId())
                .provider(provider)
                .requestId(requestId)
                .amount(order.getFinalAmount())
                .status(PaymentTransaction.Status.PENDING)
                .build();
    }
}
