package vn.vuavuive.backend.modules.shipper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.modules.shipper.dto.ShipperRequest;
import vn.vuavuive.backend.modules.shipper.dto.ShipperResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ShipperController - API quản lý tài xế giao hàng và cập nhật hành trình đơn hàng.
 */
@Tag(name = "Shippers", description = "API quản lý shipper và giao nhận hàng")
@RestController
@RequestMapping("/api/shippers")
@RequiredArgsConstructor
public class ShipperController {

    private final ShipperService shipperService;

    @Operation(summary = "[ADMIN] Đăng ký một tài xế mới")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipperResponse> createShipper(@Valid @RequestBody ShipperRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shipperService.createShipper(request));
    }

    @Operation(summary = "[ADMIN/STAFF] Lấy danh sách toàn bộ tài xế")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<ShipperResponse>> getAllShippers() {
        return ResponseEntity.ok(shipperService.getAllShippers());
    }

    @Operation(summary = "[SHIPPER] Láº¥y thÃ´ng tin profile shipper hiá»‡n táº¡i")
    @GetMapping("/me")
    @PreAuthorize("hasRole('SHIPPER')")
    public ResponseEntity<ShipperResponse> getMyProfile() {
        return ResponseEntity.ok(shipperService.getMyProfile());
    }

    @Operation(summary = "[SHIPPER/ADMIN] Cập nhật trạng thái online/offline của tài xế")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SHIPPER', 'ADMIN')")
    public ResponseEntity<ShipperResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(shipperService.updateShipperStatus(id, status));
    }

    @Operation(summary = "[ADMIN] Chỉ định tài xế giao đơn hàng")
    @PostMapping("/{id}/assign/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, String>> assignOrder(
            @PathVariable UUID id,
            @PathVariable UUID orderId) {
        shipperService.assignShipperToOrder(orderId, id);
        return ResponseEntity.ok(Map.of("message", "Gán shipper thành công"));
    }

    @Operation(summary = "[SHIPPER] Cập nhật tiến độ đơn hàng (IN_TRANSIT, DELIVERED, FAILED, RETURNED)")
    @PutMapping("/{id}/orders/{orderId}/delivery")
    @PreAuthorize("hasAnyRole('SHIPPER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> updateDeliveryStatus(
            @PathVariable UUID id,
            @PathVariable UUID orderId,
            @RequestParam String status,
            @RequestParam(required = false, defaultValue = "") String note) {
        shipperService.updateDeliveryStatus(orderId, id, status, note);
        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái giao hàng thành công"));
    }

    @Operation(summary = "[SHIPPER] Gửi tọa độ GPS lên backend realtime")
    @PostMapping("/{id}/location")
    @PreAuthorize("hasAnyRole('SHIPPER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> updateLocation(
            @PathVariable UUID id,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        shipperService.updateShipperLocation(id, latitude, longitude);
        return ResponseEntity.ok(Map.of("message", "Cập nhật tọa độ GPS thành công"));
    }
}
