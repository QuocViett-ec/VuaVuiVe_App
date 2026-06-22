package vn.vuavuive.backend.modules.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.order.OrderRepository;
import vn.vuavuive.backend.modules.order.OrderStatusLog;
import vn.vuavuive.backend.modules.order.OrderStatusLogRepository;
import vn.vuavuive.backend.modules.payment.dto.*;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoService {
    @Value("${app.payment.momo.partner-code:MOMO}") private String partnerCode;
    @Value("${app.payment.momo.access-key:}") private String accessKey;
    @Value("${app.payment.momo.secret-key:}") private String secretKey;
    @Value("${app.payment.momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}") private String endpoint;
    @Value("${app.payment.momo.redirect-url:http://10.0.2.2:3000/api/momo/return}") private String redirectUrl;
    @Value("${app.payment.momo.ipn-url:http://10.0.2.2:3000/api/momo/ipn}") private String ipnUrl;
    @Value("${app.payment.momo.request-type:captureWallet}") private String requestType;
    @Value("${app.payment.momo.lang:vi}") private String lang;
    @Value("${app.payment.momo.mock-mode:false}") private boolean mockMode;

    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final OrderStatusLogRepository statusLogRepository;

    @Transactional(rollbackFor = Exception.class)
    public CreateMomoPaymentResponse createMomoPayment(CreateMomoPaymentRequest request) {
        User user = currentUser();
        Order order = orderRepository.findById(UUID.fromString(request.orderId()))
                .orElseThrow(() -> AppException.notFound("Don hang"));
        if (!order.getUser().getId().equals(user.getId())) throw AppException.forbidden("Khong co quyen thanh toan don nay");
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) throw AppException.badRequest("Don hang da thanh toan");
        if (order.getFinalAmount() == null || order.getFinalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.badRequest("So tien thanh toan khong hop le");
        }
        if (request.amount() != null && request.amount().compareTo(order.getFinalAmount()) != 0) {
            throw AppException.badRequest("So tien thanh toan khong khop don hang");
        }

        String requestId = partnerCode + "-" + System.currentTimeMillis();
        String amount = order.getFinalAmount().toBigInteger().toString();
        String orderInfo = request.orderInfo() == null || request.orderInfo().isBlank()
                ? "Thanh toan don hang Vua Vui Ve: " + request.orderId()
                : request.orderInfo();
        String extraData = "";
        log.info("MoMo create requestId={}, orderId={}, amount={}", requestId, order.getId(), amount);

        if (mockMode) {
            return createMockPayment(order, user, requestId);
        }
        if (accessKey.isBlank() || secretKey.isBlank()) {
            throw AppException.badRequest("Chua cau hinh MoMo sandbox key hoac MOCK_MOMO_MODE=false");
        }

        String rawSignature = createRawSignature(amount, extraData, request.orderId(), orderInfo, requestId);
        String signature = hmacSHA256(secretKey, rawSignature);

        MomoCreateRequest momoRequest = new MomoCreateRequest(
                partnerCode, requestId, amount, request.orderId(), orderInfo, redirectUrl, ipnUrl,
                extraData, requestType, lang, signature);

        ResponseEntity<MomoCreateResponse> response;
        try {
            response = restTemplate.postForEntity(
                    endpoint,
                    new HttpEntity<>(momoRequest, jsonHeaders()),
                    MomoCreateResponse.class);
        } catch (RestClientException e) {
            log.error("MoMo create failed requestId={}, orderId={}, amount={}", requestId, request.orderId(), amount, e);
            throw AppException.badRequest("Khong ket noi duoc MoMo sandbox");
        }

        MomoCreateResponse body = response.getBody();
        if (body == null) throw AppException.badRequest("MoMo khong tra ve du lieu");

        PaymentTransaction tx = PaymentTransaction.builder()
                .order(order)
                .user(user)
                .provider("MOMO")
                .amount(order.getFinalAmount())
                .requestId(requestId)
                .status(PaymentTransaction.Status.PENDING)
                .payUrl(body.payUrl())
                .deeplink(body.deeplink())
                .qrCodeUrl(body.qrCodeUrl())
                .resultCode(body.resultCode())
                .message(body.message())
                .build();
        transactionRepository.save(tx);

        if (body.resultCode() == null || body.resultCode() != 0 || body.payUrl() == null) {
            tx.setStatus(PaymentTransaction.Status.FAILED);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            throw AppException.badRequest(body.message() != null ? body.message() : "Khong tao duoc thanh toan MoMo");
        }

        order.setPaymentMethod("MOMO");
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        orderRepository.save(order);

        return new CreateMomoPaymentResponse(request.orderId(), requestId, order.getFinalAmount(),
                body.payUrl(), body.deeplink(), body.qrCodeUrl(), body.resultCode(), body.message());
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleMomoIpn(MomoIpnRequest request) {
        if (!validateIpnSignature(request)) throw AppException.badRequest("MoMo signature khong hop le");

        PaymentTransaction tx = transactionRepository.findByRequestId(request.requestId())
                .orElseThrow(() -> AppException.notFound("Giao dich MoMo"));
        if (!tx.getOrder().getId().toString().equals(request.orderId())) {
            throw AppException.badRequest("Don hang MoMo khong khop");
        }
        if (tx.getAmount().toBigInteger().longValue() != request.amount()) {
            throw AppException.badRequest("So tien MoMo khong khop");
        }
        if (tx.getStatus() == PaymentTransaction.Status.PAID) return;

        Order order = tx.getOrder();
        tx.setTransactionId(request.transId() == null ? null : String.valueOf(request.transId()));
        tx.setResultCode(request.resultCode());
        tx.setMessage(request.message());
        tx.setResponseTime(request.responseTime());
        log.info("MoMo IPN requestId={}, orderId={}, amount={}, resultCode={}",
                request.requestId(), request.orderId(), request.amount(), request.resultCode());

        if (request.resultCode() != null && request.resultCode() == 0) {
            tx.setStatus(PaymentTransaction.Status.PAID);
            order.setPaymentMethod("MOMO");
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            appendStatusLog(order, "Thanh toan MoMo thanh cong");
        } else {
            tx.setStatus(PaymentTransaction.Status.FAILED);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
        }
        transactionRepository.save(tx);
        orderRepository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleMockResult(String orderId, String requestId, boolean success) {
        if (!mockMode) throw AppException.badRequest("MoMo mock mode is disabled");
        PaymentTransaction tx = transactionRepository.findByRequestId(requestId)
                .orElseThrow(() -> AppException.notFound("Giao dich MoMo"));
        if (!tx.getOrder().getId().toString().equals(orderId)) {
            throw AppException.badRequest("Don hang MoMo khong khop");
        }
        Order order = tx.getOrder();
        tx.setResultCode(success ? 0 : 1006);
        tx.setMessage(success ? "Mock MoMo success" : "Mock MoMo failed");
        tx.setResponseTime(System.currentTimeMillis());
        if (success) {
            tx.setStatus(PaymentTransaction.Status.PAID);
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            appendStatusLog(order, "Thanh toan MoMo mock thanh cong");
        } else {
            tx.setStatus(PaymentTransaction.Status.FAILED);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
        }
        log.info("MoMo mock result requestId={}, orderId={}, amount={}, resultCode={}",
                requestId, orderId, tx.getAmount(), tx.getResultCode());
        transactionRepository.save(tx);
        orderRepository.save(order);
    }

    public PaymentStatusResponse getPaymentStatus(String orderId) {
        User user = currentUser();
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> AppException.notFound("Don hang"));
        if (!order.getUser().getId().equals(user.getId())) throw AppException.forbidden("Khong co quyen xem don nay");

        PaymentTransaction tx = transactionRepository
                .findFirstByOrderAndProviderOrderByCreatedAtDesc(order, "MOMO")
                .orElse(null);
        return new PaymentStatusResponse(orderId, order.getPaymentMethod(), order.getPaymentStatus().name(),
                tx == null ? null : tx.getTransactionId(),
                tx == null ? order.getFinalAmount() : tx.getAmount(),
                tx == null ? null : tx.getMessage());
    }

    public boolean validateIpnSignature(MomoIpnRequest p) {
        if (p.signature() == null) return false;
        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + p.amount()
                + "&extraData=" + (p.extraData() == null ? "" : p.extraData())
                + "&message=" + p.message()
                + "&orderId=" + p.orderId()
                + "&orderInfo=" + p.orderInfo()
                + "&orderType=" + p.orderType()
                + "&partnerCode=" + p.partnerCode()
                + "&payType=" + p.payType()
                + "&requestId=" + p.requestId()
                + "&responseTime=" + p.responseTime()
                + "&resultCode=" + p.resultCode()
                + "&transId=" + p.transId();
        return hmacSHA256(secretKey, rawSignature).equals(p.signature());
    }

    // Backward-compatible helper for any old VNPay-like caller still compiled in this project.
    public boolean validateIpnSignature(Map<String, Object> params) {
        return validateIpnSignature(new MomoIpnRequest(
                str(params, "partnerCode"), str(params, "orderId"), str(params, "requestId"),
                lng(params, "amount"), str(params, "orderInfo"), str(params, "orderType"),
                lng(params, "transId"), integer(params, "resultCode"), str(params, "message"),
                str(params, "payType"), lng(params, "responseTime"), str(params, "extraData"),
                str(params, "signature")));
    }

    private String createRawSignature(String amount, String extraData, String orderId, String orderInfo, String requestId) {
        return "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;
    }

    private CreateMomoPaymentResponse createMockPayment(Order order, User user, String requestId) {
        String baseUrl = redirectUrl.replace("/return", "/mock").replace("localhost", "10.0.2.2");
        String url = baseUrl + "?orderId=" + order.getId() + "&requestId=" + requestId + "&amount=" + order.getFinalAmount().toBigInteger();
        PaymentTransaction tx = PaymentTransaction.builder()
                .order(order)
                .user(user)
                .provider("MOMO")
                .amount(order.getFinalAmount())
                .requestId(requestId)
                .status(PaymentTransaction.Status.PENDING)
                .payUrl(url)
                .deeplink(url)
                .qrCodeUrl(url)
                .resultCode(0)
                .message("Mock MoMo payment")
                .build();
        transactionRepository.save(tx);
        order.setPaymentMethod("MOMO");
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        orderRepository.save(order);
        return new CreateMomoPaymentResponse(order.getId().toString(), requestId, order.getFinalAmount(),
                url, url, url, 0, "Mock MoMo payment");
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .or(() -> userRepository.findByPhone(email))
                .orElseThrow(() -> AppException.notFound("User"));
    }

    private void appendStatusLog(Order order, String note) {
        statusLogRepository.save(OrderStatusLog.builder()
                .order(order)
                .status(Order.OrderStatus.CONFIRMED)
                .note(note)
                .updatedByRole("SYSTEM")
                .updatedByName("MoMo Gateway")
                .build());
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
            throw new RuntimeException("Loi tao chu ky MoMo HmacSHA256", e);
        }
    }

    private static String str(Map<String, Object> p, String key) {
        Object v = p.get(key);
        return v == null ? null : String.valueOf(v);
    }
    private static Long lng(Map<String, Object> p, String key) {
        Object v = p.get(key);
        return v == null ? null : Long.valueOf(String.valueOf(v));
    }
    private static Integer integer(Map<String, Object> p, String key) {
        Object v = p.get(key);
        return v == null ? null : Integer.valueOf(String.valueOf(v));
    }
}
