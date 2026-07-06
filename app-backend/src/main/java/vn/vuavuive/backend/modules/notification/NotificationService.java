package vn.vuavuive.backend.modules.notification;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DeviceTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public void registerCurrentUser(String token, String platform, String appScope) {
        User user = currentUser();
        tokenRepository.save(user.getId(), token, valueOrDefault(platform, "android"), valueOrDefault(appScope, "app"));
    }

    public void deleteCurrentUserToken(String token) {
        tokenRepository.delete(currentUser().getId(), token);
    }

    @Async
    public void sendToUser(String userId, String title, String body, Map<String, String> data) {
        if (userId == null || userId.isBlank()) return;
        for (DeviceToken token : tokenRepository.findActiveByUserId(userId)) {
            send(Message.builder()
                    .setToken(token.getToken())
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .putAllData(cleanData(data))
                    .setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build())
                    .build(), userId, token.getToken());
        }
    }

    @Async
    public void sendToTopic(String topic, String title, String body, Map<String, String> data) {
        String normalizedTopic = normalizeTopic(topic);
        if (normalizedTopic == null) return;
        send(Message.builder()
                .setTopic(normalizedTopic)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(cleanData(data))
                .setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build())
                .build(), "topic:" + normalizedTopic, null);
    }

    private void send(Message message, String target, String token) {
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if (token != null && e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                // ponytail: remove dead tokens here; add retry queue only if push volume grows.
                tokenRepository.delete(target, token);
            }
            log.warn("FCM send failed for {}: {}", target, e.getMessage());
        } catch (Exception e) {
            log.warn("FCM send failed for {}: {}", target, e.getMessage());
        }
    }

    private Map<String, String> cleanData(Map<String, String> data) {
        Map<String, String> out = new HashMap<>();
        if (data != null) {
            data.forEach((key, value) -> {
                if (key != null && value != null) out.put(key, value);
            });
        }
        return out;
    }

    private String normalizeTopic(String topic) {
        if (topic == null || topic.isBlank()) return null;
        String value = topic.replace("/topics/", "").trim();
        if (!value.matches("[A-Za-z0-9_.~%-]+")) {
            throw AppException.badRequest("Topic FCM khong hop le");
        }
        return value;
    }

    private String valueOrDefault(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Chua dang nhap");
        }
        String identifier = auth.getName();
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> AppException.notFound("User"));
    }
}
