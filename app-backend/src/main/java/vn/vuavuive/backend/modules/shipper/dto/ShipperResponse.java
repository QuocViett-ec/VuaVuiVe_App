package vn.vuavuive.backend.modules.shipper.dto;

import java.util.UUID;

public record ShipperResponse(
        String id,
        String fullName,
        String phone,
        String vehicleNumber,
        String currentStatus,
        Boolean isActive,
        String userId
) {}
