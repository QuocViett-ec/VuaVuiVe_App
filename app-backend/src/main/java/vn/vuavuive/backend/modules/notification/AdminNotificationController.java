package vn.vuavuive.backend.modules.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuavuive.backend.core.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/topic")
    public ResponseEntity<ApiResponse<Void>> sendTopic(@Valid @RequestBody TopicRequest request) {
        notificationService.sendToTopic(request.topic(), request.title(), request.body(), request.data());
        return ResponseEntity.ok(ApiResponse.success("Da gui thong bao topic", null));
    }

    public record TopicRequest(
            @NotBlank String topic,
            @NotBlank String title,
            @NotBlank String body,
            Map<String, String> data
    ) {}
}
