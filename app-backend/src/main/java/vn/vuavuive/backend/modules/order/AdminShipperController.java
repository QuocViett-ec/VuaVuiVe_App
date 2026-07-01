package vn.vuavuive.backend.modules.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuavuive.backend.modules.shipper.ShipperService;
import vn.vuavuive.backend.modules.shipper.dto.ShipperResponse;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shippers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminShipperController {

    private final ShipperService shipperService;

    @GetMapping
    public ResponseEntity<List<ShipperResponse>> getShippers() {
        return ResponseEntity.ok(shipperService.getAllShippers());
    }
}
