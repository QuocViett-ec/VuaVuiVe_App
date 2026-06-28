package vn.vuavuive.backend.modules.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.order.OrderItem;
import vn.vuavuive.backend.modules.order.OrderRepository;
import vn.vuavuive.backend.modules.order.OrderStatusLog;
import vn.vuavuive.backend.modules.order.OrderStatusLogRepository;
import vn.vuavuive.backend.modules.payment.dto.CreateZaloPayPaymentRequest;
import vn.vuavuive.backend.modules.payment.dto.CreateZaloPayPaymentResponse;
import vn.vuavuive.backend.modules.payment.dto.PaymentStatusResponse;
import vn.vuavuive.backend.modules.payment.dto.ZaloPayCallbackRequest;
import vn.vuavuive.backend.modules.product.Product;
import vn.vuavuive.backend.modules.product.ProductRepository;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZaloPayService {
    @Value("${app.payment.zalopay.app-id:}") private String appId;
    @Value("${app.payment.zalopay.key1:}") private String key1;
    @Value("${app.payment.zalopay.key2:}") private String key2;
    @Value("${app.payment.zalopay.endpoint:https://sb-openapi.zalopay.vn/v2/create}") private String endpoint;
    @Value("${app.payment.zalopay.query-endpoint:https://sb-openapi.zalopay.vn/v2/query}") private String queryEndpoint;
    @Value("${app.payment.zalopay.callback-url:http://10.0.2.2:3000/api/payments/zalopay/callback}") private String callbackUrl;
    @Value("${app.payment.zalopay.redirect-url:http://10.0.2.2:3000/api/payments/zalopay/return}") private String redirectUrl;
    @Value("${app.payment.zalopay.mock-mode:false}") private boolean mockMode;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final Environment environment;

    @Transactional(rollbackFor = Exception.class)
    public CreateZaloPayPaymentResponse createZaloPayPayment(CreateZaloPayPaymentRequest request) {
        User user = currentUser();
        Order order = orderRepository.findById(UUID.fromString(request.orderId()))
                .orElseThrow(() -> AppException.notFound("Don hang"));
        if (!order.getUserId().equals(user.getId())) throw AppException.forbidden("Khong co quyen thanh toan don nay");
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) throw AppException.badRequest("Don hang da thanh toan");
        if (order.getFinalAmount() == null || order.getFinalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.badRequest("So tien thanh toan khong hop le");
        }
        if (request.amount() != null && request.amount().compareTo(order.getFinalAmount()) != 0) {
            throw AppException.badRequest("So tien thanh toan khong khop don hang");
        }

        String appTransId = generateAppTransId(order.getId());
        String description = request.description() == null || request.description().isBlank()
                ? "Thanh toan don hang Vua Vui Ve: " + order.getId()
                : request.description();

        if (mockMode) {
            return createMockPayment(order, user, appTransId);
        }
        if (appId.isBlank() || key1.isBlank() || key2.isBlank()) {
            throw AppException.badRequest("Chua cau hinh ZaloPay sandbox");
        }

        long appTime = System.currentTimeMillis();
        String embedData = toJson(Map.of(
                "redirecturl", redirectUrl + "?orderId=" + order.getId(),
                "callback_url", callbackUrl,
                "orderId", order.getId()
        ));
        String item = toJson(orderItems(order));
        String amount = order.getFinalAmount().toBigInteger().toString();
        String appUser = user.getId();
        String macData = appId + "|" + appTransId + "|" + appUser + "|" + amount + "|" + appTime + "|" + embedData + "|" + item;
        String mac = hmacSHA256(key1, macData);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("app_id", appId);
        form.add("app_user", appUser);
        form.add("app_time", String.valueOf(appTime));
        form.add("amount", amount);
        form.add("app_trans_id", appTransId);
        form.add("embed_data", embedData);
        form.add("item", item);
        form.add("description", description);
        form.add("bank_code", "");
        form.add("callback_url", callbackUrl);
        form.add("mac", mac);

        Map<String, Object> body;
        try {
            body = restTemplate.postForObject(endpoint, new HttpEntity<>(form, formHeaders()), Map.class);
        } catch (RestClientException e) {
            log.error("ZaloPay create failed orderId={}, appTransId={}", order.getId(), appTransId, e);
            throw AppException.badRequest("Khong ket noi duoc ZaloPay sandbox");
        }
        if (body == null) {
            throw AppException.badRequest("ZaloPay khong tra ve du lieu");
        }

        PaymentTransaction tx = PaymentTransaction.builder()
                .orderId(order.getId())
                .userId(user.getId())
                .provider("ZALOPAY")
                .amount(order.getFinalAmount())
                .requestId(appTransId)
                .status(PaymentTransaction.Status.PENDING)
                .payUrl(stringValue(body.get("order_url")))
                .deeplink(stringValue(body.get("order_url")))
                .qrCodeUrl(stringValue(body.get("order_url")))
                .resultCode(intValue(body.get("return_code")))
                .message(stringValue(body.get("return_message")))
                .build();
        transactionRepository.save(tx);

        Integer returnCode = intValue(body.get("return_code"));
        String orderUrl = stringValue(body.get("order_url"));
        if (returnCode == null || returnCode != 1 || orderUrl == null || orderUrl.isBlank()) {
            tx.setStatus(PaymentTransaction.Status.FAILED);
            tx.setMessage(stringValue(body.get("return_message")));
            transactionRepository.save(tx);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            orderRepository.save(order);
            throw AppException.badRequest(tx.getMessage() != null ? tx.getMessage() : "Khong tao duoc thanh toan ZaloPay");
        }

        order.setPaymentMethod("ZALOPAY");
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setStatus(Order.OrderStatus.PENDING_PAYMENT);
        orderRepository.save(order);

        return new CreateZaloPayPaymentResponse(
                order.getId(),
                appTransId,
                order.getFinalAmount(),
                orderUrl,
                stringValue(body.get("zp_trans_token")),
                stringValue(body.get("qr_code")),
                returnCode,
                stringValue(body.get("return_message"))
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> handleCallback(ZaloPayCallbackRequest request) {
        if (request == null || request.data() == null || request.mac() == null) {
            return callbackResponse(-1, "invalid request");
        }
        if (!hmacSHA256(key2, request.data()).equals(request.mac())) {
            return callbackResponse(-1, "mac not equal");
        }

        Map<String, Object> payload = parsePayload(request.data());
        String appTransId = stringValue(payload.get("app_trans_id"));
        PaymentTransaction tx = transactionRepository.findByRequestId(appTransId)
                .orElseThrow(() -> AppException.notFound("Giao dich ZaloPay"));
        Order order = orderRepository.findById(tx.getOrderId())
                .orElseThrow(() -> AppException.notFound("Don hang"));
        if (tx.getStatus() == PaymentTransaction.Status.PAID) {
            return callbackResponse(1, "success");
        }

        Integer status = intValue(payload.get("status"));
        String zpTransId = stringValue(payload.get("zp_trans_id"));
        tx.setTransactionId(zpTransId);
        tx.setResponseTime(System.currentTimeMillis());

        if (status == null || status == 1) {
            tx.setStatus(PaymentTransaction.Status.PAID);
            tx.setResultCode(1);
            tx.setMessage("ZaloPay payment successful");
            order.setPaymentMethod("ZALOPAY");
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.PENDING_APPROVAL);
            appendStatusLog(order, Order.OrderStatus.PENDING_APPROVAL, "Thanh toan ZaloPay thanh cong");
        } else {
            markFailed(order, tx, "Thanh toan ZaloPay that bai", status);
        }

        transactionRepository.save(tx);
        orderRepository.save(order);
        return callbackResponse(1, "success");
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleMockResult(String orderId, boolean success) {
        ensureMockAllowed();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Don hang"));
        PaymentTransaction tx = transactionRepository
                .findFirstByOrderAndProviderOrderByCreatedAtDesc(order, "ZALOPAY")
                .orElseThrow(() -> AppException.notFound("Giao dich ZaloPay"));
        if (tx.getStatus() == PaymentTransaction.Status.PAID && success) {
            return;
        }

        tx.setResponseTime(System.currentTimeMillis());
        if (success) {
            tx.setStatus(PaymentTransaction.Status.PAID);
            tx.setResultCode(1);
            tx.setMessage("Mock ZaloPay success");
            order.setPaymentMethod("ZALOPAY");
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.PENDING_APPROVAL);
            appendStatusLog(order, Order.OrderStatus.PENDING_APPROVAL, "Thanh toan ZaloPay mock thanh cong");
        } else {
            markFailed(order, tx, "Mock ZaloPay failed", -1);
        }
        transactionRepository.save(tx);
        orderRepository.save(order);
    }

    public PaymentStatusResponse getPaymentStatus(String orderId) {
        User user = currentUser();
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> AppException.notFound("Don hang"));
        if (!order.getUserId().equals(user.getId())) throw AppException.forbidden("Khong co quyen xem don nay");

        PaymentTransaction tx = transactionRepository
                .findFirstByOrderAndProviderOrderByCreatedAtDesc(order, "ZALOPAY")
                .orElse(null);
        return new PaymentStatusResponse(
                orderId,
                order.getPaymentMethod(),
                order.getPaymentStatus().name(),
                order.getStatus().name(),
                tx == null ? null : tx.getTransactionId(),
                tx == null ? order.getFinalAmount() : tx.getAmount(),
                tx == null ? null : tx.getMessage()
        );
    }

    private void markFailed(Order order, PaymentTransaction tx, String message, Integer resultCode) {
        tx.setStatus(PaymentTransaction.Status.FAILED);
        tx.setResultCode(resultCode);
        tx.setMessage(message);
        order.setPaymentStatus(Order.PaymentStatus.FAILED);
        if (order.getStatus() != Order.OrderStatus.CANCELLED) {
            restoreStock(order);
            order.setStatus(Order.OrderStatus.CANCELLED);
            appendStatusLog(order, Order.OrderStatus.CANCELLED, message);
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            if (item.getProductId() == null) continue;
            Product product = productRepository.findById(UUID.fromString(item.getProductId())).orElse(null);
            if (product == null) continue;
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private CreateZaloPayPaymentResponse createMockPayment(Order order, User user, String appTransId) {
        String url = redirectUrl.replace("/return", "/mock")
                + "?orderId=" + order.getId()
                + "&appTransId=" + appTransId
                + "&amount=" + order.getFinalAmount().toBigInteger();
        PaymentTransaction tx = PaymentTransaction.builder()
                .orderId(order.getId())
                .userId(user.getId())
                .provider("ZALOPAY")
                .amount(order.getFinalAmount())
                .requestId(appTransId)
                .status(PaymentTransaction.Status.PENDING)
                .payUrl(url)
                .deeplink(url)
                .qrCodeUrl(url)
                .resultCode(1)
                .message("Mock ZaloPay payment")
                .build();
        transactionRepository.save(tx);
        order.setPaymentMethod("ZALOPAY");
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setStatus(Order.OrderStatus.PENDING_PAYMENT);
        orderRepository.save(order);
        return new CreateZaloPayPaymentResponse(order.getId(), appTransId, order.getFinalAmount(), url, appTransId, url, 1, "Mock ZaloPay payment");
    }

    private void ensureMockAllowed() {
        if (mockMode || environment.matchesProfiles("dev")) return;
        throw AppException.badRequest("ZaloPay mock result is disabled");
    }

    private String generateAppTransId(String orderId) {
        String date = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyMMdd"));
        return date + "_" + orderId.replace("-", "");
    }

    private List<Map<String, Object>> orderItems(Order order) {
        if (order.getOrderItems() == null) return Collections.emptyList();
        return order.getOrderItems().stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("itemid", item.getProductId());
            map.put("itemname", item.getProductName());
            map.put("itemprice", item.getUnitPrice() != null ? item.getUnitPrice().intValue() : 0);
            map.put("itemquantity", item.getQuantity());
            return map;
        }).toList();
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .or(() -> userRepository.findByPhone(email))
                .orElseThrow(() -> AppException.notFound("User"));
    }

    private HttpHeaders formHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private Map<String, Object> parsePayload(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw AppException.badRequest("Khong doc duoc callback ZaloPay");
        }
    }

    private Map<String, Object> callbackResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("return_code", code);
        response.put("return_message", message);
        return response;
    }

    private void appendStatusLog(Order order, Order.OrderStatus status, String note) {
        statusLogRepository.save(OrderStatusLog.builder()
                .orderId(order.getId())
                .status(status)
                .note(note)
                .updatedByRole("SYSTEM")
                .updatedByName("ZaloPay Gateway")
                .build());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Khong tao duoc JSON ZaloPay", e);
        }
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Loi tao chu ky ZaloPay", e);
        }
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Integer intValue(Object value) {
        if (value == null) return null;
        return Integer.valueOf(String.valueOf(value));
    }
}
