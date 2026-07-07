package vn.vuavuive.backend.modules.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.order.OrderRepository;
import vn.vuavuive.backend.modules.order.OrderStatusLogRepository;
import vn.vuavuive.backend.modules.payment.dto.PaymentStatusResponse;
import vn.vuavuive.backend.modules.payment.dto.ZaloPayCallbackRequest;
import vn.vuavuive.backend.modules.product.ProductRepository;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayStatusTest {

    @Mock RestTemplate restTemplate;
    @Spy ObjectMapper objectMapper = new ObjectMapper();
    @Mock OrderRepository orderRepository;
    @Mock PaymentTransactionRepository transactionRepository;
    @Mock OrderStatusLogRepository statusLogRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock Environment environment;

    @InjectMocks MoMoService moMoService;
    @InjectMocks ZaloPayService zaloPayService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

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
    void zaloPayCallbackRejectsAmountMismatch() throws Exception {
        Order order = onlineOrder("order-3", "ZALOPAY");
        PaymentTransaction tx = tx(order, "ZALOPAY", "req-3");
        String data = "{\"app_id\":2554,\"app_trans_id\":\"req-3\",\"amount\":21000,\"zp_trans_id\":123}";

        ReflectionTestUtils.setField(zaloPayService, "appId", "2554");
        ReflectionTestUtils.setField(zaloPayService, "key2", "secret2");
        when(transactionRepository.findByRequestId("req-3")).thenReturn(Optional.of(tx));
        when(orderRepository.findById("order-3")).thenReturn(Optional.of(order));

        Map<String, Object> response = zaloPayService.handleCallback(
                new ZaloPayCallbackRequest(data, hmacSha256("secret2", data), 1));

        assertEquals(-1, response.get("return_code"));
        assertEquals(PaymentTransaction.Status.PENDING, tx.getStatus());
        assertEquals(Order.PaymentStatus.PENDING, order.getPaymentStatus());
    }

    @Test
    void zaloPayStatusQueriesGatewayWhenPending() {
        String orderId = "00000000-0000-0000-0000-000000000004";
        User user = User.builder().email("buyer@test.local").build();
        user.setId("user-1");
        Order order = onlineOrder(orderId, "ZALOPAY");
        order.setUserId("user-1");
        PaymentTransaction tx = tx(order, "ZALOPAY", "260707_req4");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer@test.local", null));
        ReflectionTestUtils.setField(zaloPayService, "appId", "2554");
        ReflectionTestUtils.setField(zaloPayService, "key1", "secret1");
        ReflectionTestUtils.setField(zaloPayService, "key2", "secret2");
        ReflectionTestUtils.setField(zaloPayService, "mockMode", false);
        ReflectionTestUtils.setField(zaloPayService, "queryEndpoint", "https://query.zalopay.test");

        when(userRepository.findByEmail("buyer@test.local")).thenReturn(Optional.of(user));
        when(orderRepository.findById(UUID.fromString(orderId))).thenReturn(Optional.of(order));
        when(transactionRepository.findFirstByOrderAndProviderOrderByCreatedAtDesc(order, "ZALOPAY"))
                .thenReturn(Optional.of(tx));
        when(restTemplate.postForObject(eq("https://query.zalopay.test"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(Map.of(
                        "return_code", 1,
                        "return_message", "OK",
                        "amount", 20_000,
                        "zp_trans_id", 123456789L));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentStatusResponse response = zaloPayService.getPaymentStatus(orderId);

        assertEquals("PAID", response.paymentStatus());
        assertEquals(PaymentTransaction.Status.PAID, tx.getStatus());
        assertEquals("123456789", tx.getTransactionId());
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

    private static String hmacSha256(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
