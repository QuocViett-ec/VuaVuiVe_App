package vn.vuavuive.backend.modules.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.modules.order.OrderService;

import java.util.HashMap;
import java.util.Map;

/**
 * PaymentController - Tiếp nhận Webhook/IPN và Return URL từ VNPay & MoMo
 * Các endpoint này PHẢI được cấu hình permitAll trong SecurityConfig.
 */
@Tag(name = "Payment Webhooks", description = "Các API Callback từ VNPay và MoMo")
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final MoMoService moMoService;
    private final OrderService orderService;

    /**
     * VNPay IPN (Instant Payment Notification) Webhook
     * VNPay gọi API này ngầm (Server-to-Server) để cập nhật trạng thái đơn hàng.
     */
    @Operation(summary = "IPN Webhook từ VNPay (Server-to-Server)")
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        log.info("Nhận VNPay IPN: {}", params);
        Map<String, String> response = new HashMap<>();

        try {
            boolean isValid = vnPayService.validateIpnSignature(params);
            if (!isValid) {
                log.warn("VNPay IPN: Chữ ký không hợp lệ!");
                response.put("RspCode", "97");
                response.put("Message", "Signature failed");
                return ResponseEntity.ok(response);
            }

            String orderId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");

            // Cập nhật trạng thái đơn hàng qua OrderService
            orderService.handleVNPayIpn(orderId, responseCode);

            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } catch (Exception e) {
            log.error("Lỗi xử lý VNPay IPN", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * MoMo IPN (Instant Payment Notification) Webhook
     * MoMo gửi POST request chứa JSON body về link IPN đã đăng ký.
     */
    @Operation(summary = "IPN Webhook từ MoMo (Server-to-Server)")
    @PostMapping("/momo/ipn")
    public ResponseEntity<Map<String, Object>> momoIpn(@RequestBody Map<String, Object> params) {
        log.info("Nhận MoMo IPN: {}", params);
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isValid = moMoService.validateIpnSignature(params);
            if (!isValid) {
                log.warn("MoMo IPN: Chữ ký không hợp lệ!");
                response.put("resultCode", 97);
                response.put("message", "Signature failed");
                return ResponseEntity.ok(response);
            }

            String orderId = String.valueOf(params.get("orderId"));
            String resultCode = String.valueOf(params.get("resultCode"));

            // Cập nhật trạng thái đơn hàng qua OrderService
            orderService.handleMoMoIpn(orderId, resultCode);

            response.put("resultCode", 0);
            response.put("message", "Success");
        } catch (Exception e) {
            log.error("Lỗi xử lý MoMo IPN", e);
            response.put("resultCode", 99);
            response.put("message", "Unknown error");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * VNPay Return URL (Khách hàng được redirect về sau khi thanh toán xong)
     * Trả về thông báo html đơn giản cho App Android WebView bắt thông tin.
     */
    @Operation(summary = "Return URL từ VNPay (Redirect khách hàng)")
    @GetMapping("/vnpay/return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("Khách hàng quay lại từ VNPay: {}", params);
        String responseCode = params.get("vnp_ResponseCode");
        String orderId = params.get("vnp_TxnRef");

        String html;
        if ("00".equals(responseCode)) {
            html = "<html><body><h1 style='color:green;text-align:center;'>Thanh toán thành công cho đơn " + orderId + "</h1></body></html>";
        } else {
            html = "<html><body><h1 style='color:red;text-align:center;'>Thanh toán thất bại hoặc đã hủy!</h1></body></html>";
        }
        return ResponseEntity.ok(html);
    }

    /**
     * MoMo Return URL (Khách hàng được redirect về sau khi thanh toán)
     */
    @Operation(summary = "Return URL từ MoMo (Redirect khách hàng)")
    @GetMapping("/momo/return")
    public ResponseEntity<String> momoReturn(@RequestParam Map<String, String> params) {
        log.info("Khách hàng quay lại từ MoMo: {}", params);
        String resultCode = params.get("resultCode");
        String orderId = params.get("orderId");

        String html;
        if ("0".equals(resultCode)) {
            html = "<html><body><h1 style='color:green;text-align:center;'>Thanh toán thành công cho đơn " + orderId + "</h1></body></html>";
        } else {
            html = "<html><body><h1 style='color:red;text-align:center;'>Thanh toán thất bại hoặc đã hủy!</h1></body></html>";
        }
        return ResponseEntity.ok(html);
    }
}
