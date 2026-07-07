package vn.vuavuive.backend.modules.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.notification.NotificationService;
import vn.vuavuive.backend.modules.order.dto.CreateOrderRequest;
import vn.vuavuive.backend.modules.order.dto.OrderItemRequest;
import vn.vuavuive.backend.modules.order.dto.OrderResponse;
import vn.vuavuive.backend.modules.payment.MoMoService;
import vn.vuavuive.backend.modules.payment.ZaloPayService;
import vn.vuavuive.backend.modules.product.Product;
import vn.vuavuive.backend.modules.product.ProductRepository;
import vn.vuavuive.backend.modules.shipper.ShipperRepository;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServicePaymentTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock OrderStatusLogRepository statusLogRepository;
    @Mock ShipperRepository shipperRepository;
    @Mock MoMoService moMoService;
    @Mock ZaloPayService zaloPayService;
    @Mock NotificationService notificationService;

    @InjectMocks OrderService orderService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrderUsesServerPriceAndVoucherDiscount() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer@test.local", null));

        User user = User.builder().email("buyer@test.local").fullName("Buyer").phone("090").build();
        user.setId("user-1");
        Product product = Product.builder()
                .name("Rau")
                .sellingPrice(BigDecimal.valueOf(10_000))
                .stockQuantity(5)
                .unit("kg")
                .isActive(true)
                .build();
        product.setId("prod-1");

        when(userRepository.findByEmail("buyer@test.local")).thenReturn(Optional.of(user));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setDelivery(new CreateOrderRequest.DeliveryInfo("Buyer", "090", "Dĩ An"));
        request.setPayment(Map.of("method", "COD"));
        request.setVoucherCode("VUAVUIVE");
        request.setShippingFee(0D);
        request.setDiscount(999_999D);
        request.setItems(List.of(new OrderItemRequest("prod-1", 2, BigDecimal.ONE)));

        OrderResponse response = orderService.createOrder(request, "127.0.0.1");

        assertAmount("20000", response.totalAmount());
        assertAmount("30000", response.shippingFee());
        assertAmount("3000", response.discount());
        assertAmount("47000", response.finalAmount());
        assertEquals(3, product.getStockQuantity());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertAmount("10000", captor.getValue().getOrderItems().get(0).getUnitPrice());
    }

    @Test
    void markPaidOnlyAllowsDeliveredCodOrders() {
        Order pendingCod = Order.builder()
                .status(Order.OrderStatus.PENDING_APPROVAL)
                .paymentMethod("COD")
                .paymentStatus(Order.PaymentStatus.UNPAID)
                .build();
        when(orderRepository.findById("pending")).thenReturn(Optional.of(pendingCod));
        assertThrows(AppException.class, () -> orderService.markPaid("pending"));

        Order deliveredMomo = Order.builder()
                .status(Order.OrderStatus.DELIVERED)
                .paymentMethod("MOMO")
                .paymentStatus(Order.PaymentStatus.PENDING)
                .build();
        when(orderRepository.findById("momo")).thenReturn(Optional.of(deliveredMomo));
        assertThrows(AppException.class, () -> orderService.markPaid("momo"));
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
