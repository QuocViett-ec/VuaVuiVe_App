package vn.vuavuive.backend.modules.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.modules.payment.dto.CreateMomoPaymentRequest;
import vn.vuavuive.backend.modules.payment.dto.CreateMomoPaymentResponse;
import vn.vuavuive.backend.modules.payment.dto.MomoIpnRequest;

@RestController
@RequestMapping("/api/momo")
@RequiredArgsConstructor
public class MomoController {
    private final MoMoService moMoService;

    @PostMapping("/create-payment")
    public ResponseEntity<ApiResponse<CreateMomoPaymentResponse>> createPayment(
            @RequestBody CreateMomoPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(moMoService.createMomoPayment(request)));
    }

    @PostMapping("/ipn")
    public ResponseEntity<Void> ipn(@RequestBody MomoIpnRequest request) {
        moMoService.handleMomoIpn(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/return")
    public ResponseEntity<String> momoReturn(@RequestParam(required = false) String orderId) {
        return ResponseEntity.ok("<html><body style='font-family:sans-serif;text-align:center;padding:32px'>"
                + "<h1>Da nhan ket qua MoMo</h1><p>Don hang " + orderId + "</p>"
                + "<p>Quay lai ung dung de xem danh sach don hang.</p>"
                + "<p><a style='font-size:20px' href='vuavuive://orders'>Mo ung dung</a></p></body></html>");
    }

    @GetMapping("/mock")
    public ResponseEntity<String> mock(
            @RequestParam String orderId,
            @RequestParam String requestId,
            @RequestParam(required = false, defaultValue = "0") String amount) {
        String ok = "/api/momo/mock-result?success=true&orderId=" + orderId + "&requestId=" + requestId;
        String fail = "/api/momo/mock-result?success=false&orderId=" + orderId + "&requestId=" + requestId;
        return ResponseEntity.ok("<html><body style='font-family:sans-serif;text-align:center;padding:32px'>"
                + "<h1>Mock MoMo</h1><p>Order " + orderId + "</p>"
                + "<h2>" + amount + " VND</h2>"
                + "<p><a href='" + ok + "'>Success</a></p>"
                + "<p><a href='" + fail + "'>Failure</a></p></body></html>");
    }

    @GetMapping("/mock-result")
    public ResponseEntity<String> mockResult(
            @RequestParam String orderId,
            @RequestParam String requestId,
            @RequestParam boolean success) {
        moMoService.handleMockResult(orderId, requestId, success);
        return ResponseEntity.ok("<html><body style='font-family:sans-serif;text-align:center;padding:32px'>"
                + "<h1>" + (success ? "Payment successful" : "Payment failed") + "</h1>"
                + "<p>You can return to the app.</p></body></html>");
    }
}
