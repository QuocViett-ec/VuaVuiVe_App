package vn.vuavuive.backend.modules.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DeviceTokenRepository {

    private final FirebaseRepositoryHelper firebase;

    public void save(String userId, String token, String platform, String appScope) {
        String id = tokenId(token);
        DeviceToken current = firebase.get(path(userId, id), DeviceToken.class);
        DeviceToken saved = current != null ? current : new DeviceToken();
        String now = Instant.now().toString();
        saved.setId(id);
        saved.setUserId(userId);
        saved.setToken(token);
        saved.setPlatform(platform);
        saved.setAppScope(appScope);
        saved.setActive(true);
        saved.setCreatedAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : now);
        saved.setLastSeenAt(now);
        firebase.save(path(userId, id), saved);
    }

    public List<DeviceToken> findActiveByUserId(String userId) {
        return firebase.getList("device_tokens/" + userId, DeviceToken.class).stream()
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .filter(t -> t.getToken() != null && !t.getToken().isBlank())
                .toList();
    }

    public void delete(String userId, String token) {
        firebase.delete(path(userId, tokenId(token)));
    }

    private String path(String userId, String id) {
        return "device_tokens/" + userId + "/" + id;
    }

    private String tokenId(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash FCM token", e);
        }
    }
}
