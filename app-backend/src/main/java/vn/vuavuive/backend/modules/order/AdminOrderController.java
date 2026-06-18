package vn.vuavuive.backend.modules.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuavuive.backend.modules.order.dto.OrderResponse;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(orderService.getAllOrders(status, normalizePage(page), pageSize(size, limit, 50)));
    }

    private int normalizePage(int page) {
        return Math.max(page - 1, 0);
    }

    private int pageSize(Integer size, Integer limit, int defaultSize) {
        int value = limit != null ? limit : (size != null ? size : defaultSize);
        return Math.max(1, Math.min(value, 100));
    }
}
