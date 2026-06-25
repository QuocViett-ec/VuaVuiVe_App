package vn.vuavuive.backend.modules.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResendEmailService {

    private final RestTemplate restTemplate;

    @Value("${app.resend.api-key:}")
    private String apiKey;

    public void sendOtp(String email, String otpCode) {
        if (email == null || email.isEmpty()) {
            log.warn("Email is empty. OTP sending skipped.");
            return;
        }

        boolean isPlaceholderKey = apiKey == null || apiKey.isEmpty() || "re_YOUR_RESEND_API_KEY".equals(apiKey);
        if (isPlaceholderKey) {
            log.info("[RESEND EMAIL FALLBACK] Send OTP {} to email {}", otpCode, email);
            return;
        }

        try {
            String url = "https://api.resend.com/emails";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("from", "Vua Vui Ve <onboarding@resend.dev>");
            body.put("to", Collections.singletonList(email));
            body.put("subject", "[Vựa Vui Vẻ] Mã OTP đăng ký tài khoản");
            body.put("html", String.format(
                "<p>Chào bạn,</p><p>Mã OTP đăng ký tài khoản Vựa Vui Vẻ của bạn là: <strong>%s</strong>. Hiệu lực trong vòng 5 phút.</p>",
                otpCode
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, Map.class);
            log.info("OTP email sent successfully via Resend to {}", email);

        } catch (Exception e) {
            log.error("Failed to send OTP email via Resend: {}", e.getMessage());
            // Fallback to log on failure
            log.info("[RESEND EMAIL FALLBACK ON API ERROR] Send OTP {} to email {}", otpCode, email);
        }
    }
}
