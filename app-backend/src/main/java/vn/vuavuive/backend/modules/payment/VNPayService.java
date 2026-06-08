package vn.vuavuive.backend.modules.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * VNPayService — Tích hợp cổng thanh toán VNPay.
 *
 * Thuật toán chữ ký: HmacSHA512 (port từ code mẫu order.js của VNPay)
 * Môi trường: Sandbox (https://sandbox.vnpayment.vn)
 *
 * Luồng:
 * 1. App đặt hàng → Backend gọi createPaymentUrl() → Trả URL cho App
 * 2. App mở WebView với URL đó → Khách thanh toán
 * 3. VNPay gọi IPN về /api/payments/vnpay/ipn → Backend xác thực & cập nhật DB
 */
@Slf4j
@Service
public class VNPayService {

    @Value("${app.payment.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${app.payment.vnpay.secret-key}")
    private String secretKey;

    @Value("${app.payment.vnpay.pay-url}")
    private String payUrl;

    @Value("${app.payment.vnpay.return-url}")
    private String returnUrl;

    /**
     * Tạo URL thanh toán VNPay.
     *
     * @param orderId   ID đơn hàng (dùng làm vnp_TxnRef)
     * @param amount    Số tiền VND (ví dụ: 150000)
     * @param clientIp  IP của khách hàng
     * @return URL thanh toán — App mở trong WebView
     */
    public String createPaymentUrl(String orderId, BigDecimal amount, String clientIp) {
        // VNPay yêu cầu thời gian theo múi giờ Asia/Ho_Chi_Minh
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String createDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(fmt);

        // Build params theo đúng thứ tự (quan trọng cho việc tạo chữ ký)
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Locale", "vn");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
        params.put("vnp_OrderType", "other");
        // VNPay yêu cầu amount * 100 (đơn vị: xu)
        params.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_CreateDate", createDate);

        // Tạo chuỗi ký (signData) — Encode từng giá trị
        StringBuilder signData = new StringBuilder();
        StringBuilder queryData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8).replace("+", "%20");
            signData.append(entry.getKey()).append("=").append(encodedValue).append("&");
            queryData.append(entry.getKey()).append("=").append(encodedValue).append("&");
        }
        // Xóa dấu & cuối
        signData.deleteCharAt(signData.length() - 1);
        queryData.deleteCharAt(queryData.length() - 1);

        // Ký HmacSHA512 (giống hệt code mẫu NodeJS của VNPay)
        String secureHash = hmacSHA512(secretKey, signData.toString());
        queryData.append("&vnp_SecureHash=").append(secureHash);

        String finalUrl = payUrl + "?" + queryData;
        log.info("VNPay URL tạo cho đơn {}: {}", orderId, finalUrl);
        return finalUrl;
    }

    /**
     * Xác thực chữ ký IPN từ VNPay (Instant Payment Notification).
     * VNPay gọi API này sau khi khách thanh toán thành công/thất bại.
     *
     * @param params Toàn bộ query params từ VNPay gửi về
     * @return true nếu chữ ký hợp lệ
     */
    public boolean validateIpnSignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        // Xóa các trường hash ra khỏi params trước khi tính lại chữ ký
        Map<String, String> checkParams = new TreeMap<>(params);
        checkParams.remove("vnp_SecureHash");
        checkParams.remove("vnp_SecureHashType");

        StringBuilder signData = new StringBuilder();
        for (Map.Entry<String, String> entry : checkParams.entrySet()) {
            signData.append(entry.getKey()).append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8).replace("+", "%20"))
                    .append("&");
        }
        signData.deleteCharAt(signData.length() - 1);

        String expectedHash = hmacSHA512(secretKey, signData.toString());
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    /** Thuật toán ký HmacSHA512 — Port từ code mẫu VNPay NodeJS */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // Convert byte array to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký VNPay HmacSHA512", e);
        }
    }
}
