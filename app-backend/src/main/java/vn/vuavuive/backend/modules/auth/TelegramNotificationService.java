package vn.vuavuive.backend.modules.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService {

    private final RestTemplate restTemplate;
    private final Environment environment;

    @Value("${app.telegram.bot-token:}")
    private String botToken;

    @Value("${app.telegram.chat-id:}")
    private String chatId;

    public void sendOtp(String phone, String otpCode) {
        String maskedPhone = maskPhone(phone);
        String message = String.format("[Vựa Vui Vẻ] OTP đăng ký cho %s: %s. Hiệu lực 5 phút.", maskedPhone, otpCode);

        if (botToken == null || botToken.isEmpty() || chatId == null || chatId.isEmpty()) {
            if (isDevOrLocalProfile()) {
                log.info("[TELEGRAM BOT FALLBACK] Send OTP {} to phone {}", otpCode, phone);
            } else {
                log.warn("Telegram bot token or chat ID is not configured. OTP sending skipped.");
            }
            return;
        }

        try {
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", chatId);
            body.put("text", message);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, Map.class);
            log.info("OTP sent to Telegram chat ID {} for phone {}", chatId, maskedPhone);

        } catch (Exception e) {
            log.error("Failed to send OTP message to Telegram: {}", e.getMessage());
            // Fallback to log on API request failure during local development
            if (isDevOrLocalProfile()) {
                log.info("[TELEGRAM BOT FALLBACK ON API ERROR] Send OTP {} to phone {}", otpCode, phone);
            }
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 3) return "***";
        int len = phone.length();
        return "*******" + phone.substring(len - 3);
    }

    private boolean isDevOrLocalProfile() {
        if (environment == null) return true;
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(profile) || "local".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
