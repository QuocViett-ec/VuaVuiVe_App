package vn.vuavuive.backend.modules.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuavuive.backend.core.ApiResponse;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/device-token")
    public ResponseEntity<ApiResponse<Void>> registerDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        notificationService.registerCurrentUser(request.token(), request.platform(), request.appScope());
        return ResponseEntity.ok(ApiResponse.success("Da luu FCM token", null));
    }

    @DeleteMapping("/device-token")
    public ResponseEntity<ApiResponse<Void>> deleteDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        notificationService.deleteCurrentUserToken(request.token());
        return ResponseEntity.ok(ApiResponse.success("Da xoa FCM token", null));
    }

    public record DeviceTokenRequest(
            @NotBlank String token,
            String platform,
            String appScope
    ) {}
}
