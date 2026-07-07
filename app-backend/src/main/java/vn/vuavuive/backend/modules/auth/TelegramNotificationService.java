package vn.vuavuive.backend.modules.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import vn.vuavuive.backend.exception.AppException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService {

    private final RestTemplate restTemplate;
    @Value("${app.telegram.bot-token:}")
    private String botToken;

    @Value("${app.telegram.chat-id:}")
    private String chatId;

    public void sendOtp(String phone, String otpCode) {
        sendOtp(phone, otpCode, "REGISTER");
    }

    public void sendOtp(String phone, String otpCode, String type) {
        String maskedPhone = phone != null && phone.contains("@") ? phone : maskPhone(phone);
        String action = "REGISTER".equalsIgnoreCase(type) ? "đăng ký" : "khôi phục mật khẩu";
        String message = String.format("[Vựa Vui Vẻ] OTP %s cho %s: %s. Hiệu lực 5 phút.", action, maskedPhone, otpCode);

        if (botToken == null || botToken.trim().isEmpty() || chatId == null || chatId.trim().isEmpty()) {
            log.error("Telegram bot token or chat ID is not configured. OTP was not sent.");
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình Telegram Bot để gửi OTP");
        }

        try {
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken.trim());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", chatId.trim());
            body.put("text", message);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Telegram API returned non-success status {} while sending OTP", response.getStatusCode());
                throw new AppException(HttpStatus.BAD_GATEWAY, "Telegram không nhận yêu cầu gửi OTP");
            }
            log.info("OTP sent to Telegram chat ID {} for phone {}", chatId, maskedPhone);

        } catch (RestClientResponseException e) {
            log.error("Failed to send OTP message to Telegram. HTTP {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new AppException(HttpStatus.BAD_GATEWAY, "Không gửi được OTP qua Telegram. Vui lòng thử lại");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send OTP message to Telegram: {}", e.getMessage());
            throw new AppException(HttpStatus.BAD_GATEWAY, "Không gửi được OTP qua Telegram. Vui lòng thử lại");
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 3) return "***";
        int len = phone.length();
        return "*******" + phone.substring(len - 3);
    }
}
