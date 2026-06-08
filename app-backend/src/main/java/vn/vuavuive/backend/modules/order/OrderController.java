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
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        // Lấy IP khách hàng để truyền vào VNPay
        String clientIp = httpRequest.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = httpRequest.getRemoteAddr();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, clientIp));
    }

    @Operation(summary = "Xem lịch sử đơn hàng của tôi (phân trang)")
    @GetMapping("/my")
    public ResponseEntity<PagedResponse<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getMyOrders(page, size));
    }

    @Operation(summary = "Xem chi tiết đơn hàng kèm Timeline trạng thái")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Hủy đơn hàng (Chỉ được khi đơn đang PENDING)")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @Operation(summary = "[ADMIN] Cập nhật trạng thái đơn hàng")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String newStatus   = body.get("status");
        String note        = body.getOrDefault("note", "");
        String updatedBy   = body.getOrDefault("updatedBy", "Admin");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, newStatus, note, updatedBy));
    }
}
