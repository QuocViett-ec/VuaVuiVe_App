package vn.vuavuive.backend.modules.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.modules.order.dto.OrderResponse;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;
import vn.vuavuive.backend.modules.shipper.ShipperService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ShipperService shipperService;

    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(orderService.getAllOrders(status, normalizePage(page), pageSize(size, limit, 50)));
    }

    @PatchMapping("/{id}/assign-shipper")
    public ResponseEntity<ApiResponse<Map<String, String>>> assignShipper(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        shipperService.assignShipperToOrder(id, body.get("shipperId"));
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> vn.vuavuive.backend.exception.AppException.notFound("Đơn hàng"));
        Map<String, String> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("shipperId", order.getShipperId());
        data.put("shipperName", order.getShipperName());
        data.put("status", order.getStatus().name());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    private int normalizePage(int page) {
        return Math.max(page - 1, 0);
    }

    private int pageSize(Integer size, Integer limit, int defaultSize) {
        int value = limit != null ? limit : (size != null ? size : defaultSize);
        return Math.max(1, Math.min(value, 100));
    }
}
