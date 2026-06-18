package vn.vuavuive.backend.modules.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.modules.order.dto.CreateOrderRequest;
import vn.vuavuive.backend.modules.order.dto.OrderResponse;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;

import java.util.Map;
import java.util.UUID;

/**
 * OrderController — API đặt hàng và quản lý đơn.
 *
 * CUSTOMER (Cần đăng nhập):
 *   POST   /api/orders                         — Tạo đơn hàng mới
 *   GET    /api/orders/my                      — Lịch sử đơn của tôi
 *   GET    /api/orders/{id}                    — Chi tiết đơn
 *   PATCH  /api/orders/{id}/cancel             — Hủy đơn
 *
 * SHIPPER:
 *   GET    /api/orders/shipper                 — Đơn hàng được gán cho tôi
 *
 * ADMIN/STAFF:
 *   GET    /api/orders                         — Tất cả đơn (có lọc)
 *   PATCH  /api/orders/{id}/status             — Cập nhật trạng thái
 */
@Tag(name = "Orders", description = "API tạo và quản lý đơn hàng")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Tạo đơn hàng mới — Trả về paymentUrl nếu dùng VNPay/MoMo")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = httpRequest.getRemoteAddr();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.createOrder(request, clientIp)));
    }

    @Operation(summary = "Xem lịch sử đơn hàng của tôi (phân trang)")
    @GetMapping({"/my", "/me"})
    public ResponseEntity<PagedResponse<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(orderService.getMyOrders(status, normalizePage(page), pageSize(size, limit, 10)));
    }

    @Operation(summary = "[SHIPPER] Lấy danh sách đơn hàng được gán cho Shipper hiện tại")
    @GetMapping("/shipper")
    @PreAuthorize("hasAnyRole('SHIPPER', 'ADMIN')")
    public ResponseEntity<PagedResponse<OrderResponse>> getShipperOrders(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(orderService.getShipperOrders(status, normalizePage(page), pageSize(size, limit, 20)));
    }

    @Operation(summary = "Xem chi tiết đơn hàng kèm Timeline trạng thái")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id)));
    }

    @Operation(summary = "Hủy đơn hàng (Chỉ được khi đơn đang PENDING)")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(id)));
    }

    @Operation(summary = "[ADMIN] Cập nhật trạng thái đơn hàng")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        String note      = body.getOrDefault("note", "");
        String updatedBy = body.getOrDefault("updatedBy", "Admin");
        return ResponseEntity.ok(ApiResponse.success(orderService.updateOrderStatus(id, newStatus, note, updatedBy)));
    }

    @Operation(summary = "[ADMIN] Cáº­p nháº­t tráº¡ng thÃ¡i Ä‘Æ¡n hÃ ng")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatusPut(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return updateStatus(id, body);
    }

    @PatchMapping("/{id}/paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> markPaid(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.markPaid(id)));
    }

    @PatchMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> markRefunded(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.markRefunded(id)));
    }

    private int normalizePage(int page) {
        return Math.max(page - 1, 0);
    }

    private int pageSize(Integer size, Integer limit, int defaultSize) {
        int value = limit != null ? limit : (size != null ? size : defaultSize);
        return Math.max(1, Math.min(value, 100));
    }
}
