package vn.vuavuive.backend.modules.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.modules.order.OrderService;
import vn.vuavuive.backend.modules.payment.dto.CreateMomoPaymentRequest;
import vn.vuavuive.backend.modules.payment.dto.CreateMomoPaymentResponse;
import vn.vuavuive.backend.modules.payment.dto.MomoIpnRequest;
import vn.vuavuive.backend.modules.payment.dto.PaymentStatusResponse;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Payments", description = "VNPay callbacks and MoMo sandbox payment APIs")
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayService vnPayService;
    private final MoMoService moMoService;
    private final OrderService orderService;

    @Operation(summary = "Tao thanh toan MoMo sandbox")
    @PostMapping("/momo")
    public ResponseEntity<ApiResponse<CreateMomoPaymentResponse>> createMomoPayment(
            @RequestBody CreateMomoPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(moMoService.createMomoPayment(request)));
    }

    @Operation(summary = "VNPay IPN")
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        try {
            if (!vnPayService.validateIpnSignature(params)) {
                response.put("RspCode", "97");
                response.put("Message", "Signature failed");
                return ResponseEntity.ok(response);
            }
            orderService.handleVNPayIpn(params.get("vnp_TxnRef"), params.get("vnp_ResponseCode"));
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } catch (Exception e) {
            log.error("Loi xu ly VNPay IPN", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "MoMo IPN")
    @PostMapping("/momo/ipn")
    public ResponseEntity<Void> momoIpn(@RequestBody MomoIpnRequest request) {
        moMoService.handleMomoIpn(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "VNPay return")
    @GetMapping("/vnpay/return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        String orderId = params.get("vnp_TxnRef");
        String text = "00".equals(responseCode) ? "VNPay payment returned" : "VNPay payment failed or cancelled";
        return ResponseEntity.ok("<html><body><h1 style='text-align:center;'>" + text
                + "</h1><p style='text-align:center;'>Order " + orderId + "</p></body></html>");
    }

    @Operation(summary = "MoMo return")
    @GetMapping("/momo/return")
    public ResponseEntity<String> momoReturn(@RequestParam Map<String, String> params) {
        String orderId = params.get("orderId");
        return ResponseEntity.ok("<html><body><h1 style='text-align:center;'>MoMo payment returned</h1>"
                + "<p style='text-align:center;'>Order " + orderId + ". You can close this page.</p></body></html>");
    }

    @Operation(summary = "Lay trang thai thanh toan")
    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> paymentStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(moMoService.getPaymentStatus(orderId)));
    }
}
