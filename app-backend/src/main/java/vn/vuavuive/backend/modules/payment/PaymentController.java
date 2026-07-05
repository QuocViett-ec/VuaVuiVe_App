package vn.vuavuive.backend.modules.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.order.OrderRepository;
import vn.vuavuive.backend.modules.order.OrderService;
import vn.vuavuive.backend.modules.payment.dto.CreateMomoPaymentRequest;
import vn.vuavuive.backend.modules.payment.dto.CreateMomoPaymentResponse;
import vn.vuavuive.backend.modules.payment.dto.CreateZaloPayPaymentRequest;
import vn.vuavuive.backend.modules.payment.dto.CreateZaloPayPaymentResponse;
import vn.vuavuive.backend.modules.payment.dto.MomoIpnRequest;
import vn.vuavuive.backend.modules.payment.dto.PaymentStatusResponse;
import vn.vuavuive.backend.modules.payment.dto.ZaloPayCallbackRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Payments", description = "MoMo and ZaloPay payment APIs")
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final MoMoService moMoService;
    private final ZaloPayService zaloPayService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Operation(summary = "Tao thanh toan MoMo sandbox")
    @PostMapping("/momo")
    public ResponseEntity<ApiResponse<CreateMomoPaymentResponse>> createMomoPayment(
            @RequestBody CreateMomoPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(moMoService.createMomoPayment(request)));
    }

    @Operation(summary = "Tao thanh toan ZaloPay sandbox")
    @PostMapping("/zalopay")
    public ResponseEntity<ApiResponse<CreateZaloPayPaymentResponse>> createZaloPayPayment(
            @RequestBody CreateZaloPayPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(zaloPayService.createZaloPayPayment(request)));
    }

    @Operation(summary = "MoMo IPN")
    @PostMapping("/momo/ipn")
    public ResponseEntity<Map<String, Object>> momoIpn(@RequestBody MomoIpnRequest request) {
        moMoService.handleMomoIpn(request);
        Map<String, Object> response = new HashMap<>();
        response.put("resultCode", 0);
        response.put("message", "Confirm Success");
        response.put("orderId", request.orderId());
        response.put("requestId", request.requestId());
        response.put("responseTime", Instant.now().toEpochMilli());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "MoMo return")
    @GetMapping("/momo/return")
    public ResponseEntity<String> momoReturn(@RequestParam Map<String, String> params) {
        String orderId = params.get("orderId");
        return ResponseEntity.ok("<html><body style='font-family:sans-serif;text-align:center;padding:32px'>"
                + "<h1>Da nhan ket qua MoMo</h1><p>Don hang " + orderId + "</p>"
                + "<p>Quay lai ung dung de xem danh sach don hang.</p>"
                + "<p><a style='font-size:20px' href='vuavuive://orders'>Mo ung dung</a></p></body></html>");
    }

    @Operation(summary = "ZaloPay callback")
    @PostMapping("/zalopay/callback")
    public ResponseEntity<Map<String, Object>> zaloPayCallback(@RequestBody ZaloPayCallbackRequest request) {
        return ResponseEntity.ok(zaloPayService.handleCallback(request));
    }

    @Operation(summary = "ZaloPay return")
    @GetMapping("/zalopay/return")
    public ResponseEntity<String> zaloPayReturn(@RequestParam Map<String, String> params) {
        String orderId = params.get("orderId");
        return ResponseEntity.ok("<html><body style='font-family:sans-serif;text-align:center;padding:32px'>"
                + "<h1>Da nhan ket qua ZaloPay</h1><p>Don hang " + orderId + "</p>"
                + "<p>Quay lai ung dung de xem danh sach don hang.</p>"
                + "<p><a style='font-size:20px' href='vuavuive://orders'>Mo ung dung</a></p></body></html>");
    }

    @Operation(summary = "MoMo mock screen")
    @GetMapping("/momo/mock")
    public ResponseEntity<String> momoMock(
            @RequestParam String orderId,
            @RequestParam String requestId,
            @RequestParam(required = false, defaultValue = "0") String amount) {
        String ok = "/api/payments/momo/mock-result?success=true&orderId=" + orderId + "&requestId=" + requestId;
        String fail = "/api/payments/momo/mock-result?success=false&orderId=" + orderId + "&requestId=" + requestId;
        return ResponseEntity.ok("<html><body style='font-family:sans-serif;text-align:center;padding:32px'>"
                + "<h1>Mock MoMo</h1><p>Order " + orderId + "</p>"
                + "<h2>" + amount + " VND</h2>"
                + "<p><a href='" + ok + "'>Success</a></p>"
                + "<p><a href='" + fail + "'>Failure</a></p></body></html>");
    }

    @Operation(summary = "MoMo mock result screen")
    @GetMapping("/momo/mock-result")
    public ResponseEntity<String> momoMockResult(
            @RequestParam String orderId,
            @RequestParam String requestId,
            @RequestParam boolean success) {
        moMoService.handleMockResult(orderId, requestId, success);
        return ResponseEntity.ok("<html><body style='font-family:sans-serif;text-align:center;padding:32px'>"
                + "<h1>" + (success ? "Payment successful" : "Payment failed") + "</h1>"
                + "<p>You can return to the app.</p></body></html>");
    }

    @Operation(summary = "ZaloPay mock screen")
    @GetMapping("/zalopay/mock")
    public ResponseEntity<String> zaloPayMock(
            @RequestParam String orderId,
            @RequestParam String appTransId,
            @RequestParam(required = false, defaultValue = "0") String amount) {
        String ok = "/api/payments/zalopay/mock-result?success=true&orderId=" + orderId;
        String fail = "/api/payments/zalopay/mock-result?success=false&orderId=" + orderId;
        return ResponseEntity.ok("<html><body style='font-family:sans-serif;text-align:center;padding:32px'>"
                + "<h1>Mock ZaloPay</h1><p>Order " + orderId + "</p>"
                + "<p>AppTransId " + appTransId + "</p>"
                + "<h2>" + amount + " VND</h2>"
                + "<p><a href='" + ok + "'>Success</a></p>"
                + "<p><a href='" + fail + "'>Failure</a></p></body></html>");
    }

    @Operation(summary = "ZaloPay mock result screen")
    @GetMapping("/zalopay/mock-result")
    public ResponseEntity<String> zaloPayMockResult(
            @RequestParam String orderId,
            @RequestParam boolean success) {
        zaloPayService.handleMockResult(orderId, success);
        return ResponseEntity.ok("<html><body style='font-family:sans-serif;text-align:center;padding:32px'>"
                + "<h1>" + (success ? "Payment successful" : "Payment failed") + "</h1>"
                + "<p>You can return to the app.</p></body></html>");
    }

    @Operation(summary = "Lay trang thai thanh toan")
    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> paymentStatus(@PathVariable String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> vn.vuavuive.backend.exception.AppException.notFound("Don hang"));
        PaymentStatusResponse response = "ZALOPAY".equalsIgnoreCase(order.getPaymentMethod())
                ? zaloPayService.getPaymentStatus(orderId)
                : moMoService.getPaymentStatus(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Dev-only mock MoMo success")
    @PostMapping("/momo/mock-success/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mockMomoSuccess(@PathVariable String orderId) {
        moMoService.handleMockResult(orderId, true);
        return ResponseEntity.ok(ApiResponse.success(mockStatus(orderId)));
    }

    @Operation(summary = "Dev-only mock MoMo fail")
    @PostMapping("/momo/mock-fail/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mockMomoFail(@PathVariable String orderId) {
        moMoService.handleMockResult(orderId, false);
        return ResponseEntity.ok(ApiResponse.success(mockStatus(orderId)));
    }

    @Operation(summary = "Dev-only mock ZaloPay success")
    @PostMapping("/zalopay/mock-success/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mockZaloPaySuccess(@PathVariable String orderId) {
        zaloPayService.handleMockResult(orderId, true);
        return ResponseEntity.ok(ApiResponse.success(mockStatus(orderId)));
    }

    @Operation(summary = "Dev-only mock ZaloPay fail")
    @PostMapping("/zalopay/mock-fail/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mockZaloPayFail(@PathVariable String orderId) {
        zaloPayService.handleMockResult(orderId, false);
        return ResponseEntity.ok(ApiResponse.success(mockStatus(orderId)));
    }

    private Map<String, Object> mockStatus(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> vn.vuavuive.backend.exception.AppException.notFound("Don hang"));
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("paymentMethod", order.getPaymentMethod());
        data.put("paymentStatus", order.getPaymentStatus().name());
        data.put("status", order.getStatus().name());
        data.put("amount", order.getFinalAmount());
        return data;
    }
}
