package vn.vuavuive.backend.modules.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * MoMoService — Tích hợp cổng thanh toán MoMo.
 *
 * Thuật toán chữ ký: HmacSHA256 (port từ code mẫu MoMo.js)
 * requestType: "captureWallet" (thanh toán qua Ví MoMo)
 * Môi trường: Sandbox (https://test-payment.momo.vn)
 *
 * Luồng:
 * 1. Backend gọi API MoMo → MoMo trả về payUrl
 * 2. App mở WebView với payUrl → Khách xác nhận trên app MoMo
 * 3. MoMo gọi IPN về /api/payments/momo/ipn → Backend xác thực & cập nhật DB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoService {

    @Value("${app.payment.momo.partner-code}")
    private String partnerCode;

    @Value("${app.payment.momo.access-key}")
    private String accessKey;

    @Value("${app.payment.momo.secret-key}")
    private String secretKey;

    @Value("${app.payment.momo.endpoint}")
    private String endpoint;

    @Value("${app.payment.momo.return-url}")
    private String returnUrl;

    @Value("${app.payment.momo.notify-url}")
    private String notifyUrl;

    private final RestTemplate restTemplate;

    /**
     * Tạo yêu cầu thanh toán MoMo và trả về payUrl.
     *
     * @param orderId ID đơn hàng
     * @param amount  Số tiền VND
     * @return payUrl — App mở trong WebView
     */
    public String createPaymentUrl(String orderId, BigDecimal amount) {
        String requestId = partnerCode + System.currentTimeMillis();
        String orderInfo = "Thanh toan don hang Vua Vui Ve: " + orderId;
        String extraData = "";
        String requestType = "captureWallet";

        // Chuỗi ký theo đúng format của MoMo (thứ tự alphabet của field name)
        // accessKey=...&amount=...&extraData=...&ipnUrl=...&orderId=...&orderInfo=...
        // &partnerCode=...&redirectUrl=...&requestId=...&requestType=...
        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount.toBigInteger()
                + "&extraData=" + extraData
                + "&ipnUrl=" + notifyUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + returnUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        String signature = hmacSHA256(secretKey, rawSignature);
        log.debug("MoMo rawSignature: {}", rawSignature);
        log.debug("MoMo signature: {}", signature);

        // Build JSON body gửi lên MoMo
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("accessKey", accessKey);
        body.put("requestId", requestId);
        body.put("amount", amount.toBigInteger().toString());
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", returnUrl);
        body.put("ipnUrl", notifyUrl);
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", signature);
        body.put("lang", "vi");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);
            if (response.getBody() != null) {
                String payUrl = (String) response.getBody().get("payUrl");
                String resultCode = String.valueOf(response.getBody().get("resultCode"));
                log.info("MoMo response cho đơn {}: resultCode={}, payUrl={}", orderId, resultCode, payUrl);
                if ("0".equals(resultCode) && payUrl != null) {
                    return payUrl;
                }
                log.error("MoMo trả lỗi: {}", response.getBody().get("message"));
            }
        } catch (Exception e) {
            log.error("Lỗi gọi API MoMo: ", e);
        }
        throw new RuntimeException("Không thể tạo phiên thanh toán MoMo");
    }

    /**
     * Xác thực chữ ký IPN từ MoMo.
     * MoMo gọi về endpoint này sau khi khách thanh toán.
     */
    public boolean validateIpnSignature(Map<String, Object> params) {
        String receivedSignature = (String) params.get("signature");

        // Rebuild chuỗi ký theo format MoMo IPN
        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + params.get("amount")
                + "&extraData=" + params.getOrDefault("extraData", "")
                + "&message=" + params.get("message")
                + "&orderId=" + params.get("orderId")
                + "&orderInfo=" + params.get("orderInfo")
                + "&orderType=" + params.get("orderType")
                + "&partnerCode=" + params.get("partnerCode")
                + "&payType=" + params.get("payType")
                + "&requestId=" + params.get("requestId")
                + "&responseTime=" + params.get("responseTime")
                + "&resultCode=" + params.get("resultCode")
                + "&transId=" + params.get("transId");

        String expectedSignature = hmacSHA256(secretKey, rawSignature);
        return expectedSignature.equals(receivedSignature);
    }

    /** Thuật toán ký HmacSHA256 — Port từ code mẫu MoMo.js */
    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký MoMo HmacSHA256", e);
        }
    }
}
