package vn.vuavuive.backend.modules.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GeminiService - Kết nối trực tiếp với Google Gemini AI API để cung cấp Chatbot tư vấn.
 * Sử dụng model cấu hình trong application-dev.yml (ví dụ: gemini-1.5-flash).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${app.gemini.api-key}")
    private String apiKey;

    @Value("${app.gemini.model}")
    private String model;

    private final RestTemplate restTemplate;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    /**
     * Gửi tin nhắn đến Gemini AI và nhận về câu trả lời.
     * Cung cấp ngữ cảnh là trợ lý ảo của "Vựa Vui Vẻ".
     */
    public String chatWithBot(String userPrompt) {
        String url = String.format(GEMINI_API_URL, model, apiKey);

        // System Instruction: Định hướng cho Gemini đóng vai trợ lý Vựa Vui Vẻ
        String systemInstruction = "Bạn là trợ lý ảo thông minh, thân thiện của ứng dụng 'Vựa Vui Vẻ' "
                + "- một nền tảng thương mại điện tử chuyên cung cấp thực phẩm tươi sống, rau củ quả hữu cơ và nông sản sạch sạch chuẩn VietGAP. "
                + "Nhiệm vụ của bạn là tư vấn các sản phẩm tươi sạch, gợi ý thực đơn món ăn, hướng dẫn chế biến các món ngon từ rau củ, "
                + "và giải đáp các thắc mắc về dinh dưỡng, bảo quản thực phẩm. "
                + "Hãy trả lời một cách lịch sự, vui vẻ, súc tích và luôn ưu tiên ngôn ngữ tiếng Việt.";

        // Build Request Body cho Gemini API
        Map<String, Object> requestBody = new HashMap<>();

        // Cấu hình nội dung chat
        Map<String, Object> contentMap = new HashMap<>();
        Map<String, Object> partMap = new HashMap<>();
        partMap.put("text", systemInstruction + "\n\nNgười dùng hỏi: " + userPrompt);
        contentMap.put("parts", List.of(partMap));
        requestBody.put("contents", List.of(contentMap));

        // Cấu hình các tham số generate (tùy chọn)
        Map<String, Object> safetySettings = new HashMap<>();
        // Có thể thêm safetySettings hoặc generationConfig nếu cần thiết

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Đang gọi Gemini AI API ({}) cho prompt: {}", model, userPrompt);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getBody() != null && response.getBody().containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (!parts.isEmpty()) {
                        String reply = (String) parts.get(0).get("text");
                        log.info("Gemini AI phản hồi thành công.");
                        return reply;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi kết nối với Gemini AI API: ", e);
            return "Xin lỗi bạn, kết nối với trợ lý thông minh đang gặp chút sự cố. Bạn vui lòng thử lại sau giây lát nhé!";
        }

        return "Hiện tại tôi chưa hiểu ý của bạn, vui lòng đặt câu hỏi rõ hơn về sản phẩm hữu cơ nhé!";
    }
}
