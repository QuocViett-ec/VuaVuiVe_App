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
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.vision.VisionSearchResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GeminiService - Kết nối Google Gemini AI API để cung cấp Chatbot tư vấn Vựa Vui Vẻ.
 * Nếu API lỗi, tự động fallback sang mock data thông minh tiếng Việt.
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

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SYSTEM_PROMPT =
            "Bạn là VuiVe Bot - trợ lý ảo thông minh của Vựa Vui Vẻ, nền tảng thương mại điện tử " +
            "chuyên cung cấp thực phẩm tươi sạch, rau củ quả đạt chuẩn VietGAP, thịt cá hải sản tươi ngon.\n\n" +
            "Nhiệm vụ: Tư vấn sản phẩm, gợi ý món ăn, hỗ trợ đơn hàng, giao hàng, thanh toán.\n" +
            "Voucher hiện có: VUAVUIVE giảm 15%, FREESHIP24 miễn phí ship.\n" +
            "Phong cách: Thân thiện, tiếng Việt, ngắn gọn, dùng emoji.";

    /**
     * Gửi tin nhắn đến Gemini AI, nếu lỗi thì dùng mock data thông minh.
     */
    public String chatWithBot(String userPrompt) {
        log.info("VuiVe Bot nhận prompt: {}", userPrompt);

        // Thử gọi Gemini API thật trước
        try {
            String reply = callGeminiApi(userPrompt);
            if (reply != null && !reply.isBlank()) {
                log.info("Gemini AI trả lời thành công.");
                return reply;
            }
        } catch (Exception e) {
            log.warn("Gemini API lỗi ({}), chuyển sang mock data.", e.getMessage());
        }

        // Fallback: mock data thông minh
        return getMockReply(userPrompt);
    }

    public VisionSearchResponse analyzeProductImage(String base64Image, String mimeType) {
        if (apiKey == null || apiKey.isBlank()) {
            throw AppException.badRequest("Chua cau hinh GEMINI_API_KEY trong app-backend\\.env");
        }
        try {
            String text = callGeminiVisionApi(base64Image, mimeType);
            String json = extractJson(text);
            return MAPPER.readValue(json, VisionSearchResponse.class);
        } catch (Exception e) {
            log.warn("Gemini Vision failed: {}", e.getMessage());
            throw AppException.badRequest("Khong nhan dien duoc san pham trong anh");
        }
    }

    // ── Gọi Gemini API thật ─────────────────────────────────────────────────
    private String callGeminiApi(String userPrompt) {
        String url = String.format(GEMINI_API_URL, model, apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> systemInstruction = new HashMap<>();
        systemInstruction.put("parts", List.of(Map.of("text", SYSTEM_PROMPT)));

        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        userContent.put("parts", List.of(Map.of("text", userPrompt)));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 512);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("system_instruction", systemInstruction);
        requestBody.put("contents", List.of(userContent));
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response =
                restTemplate.postForEntity(url, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                if (content != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        }
        return null;
    }

    private String callGeminiVisionApi(String base64Image, String mimeType) {
        String url = String.format(GEMINI_API_URL, model, apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = """
                Analyze this fresh food/product image for an ecommerce search feature.
                Return ONLY one valid JSON object:
                {
                  "keyword": "short Vietnamese search keyword, e.g. ca chua",
                  "keywords": ["Vietnamese and English search keywords"],
                  "category": "vegetable|fruit|meat|seafood|drink|other",
                  "confidence": 0.0
                }
                Prefer product names used in Vietnam. If the image is not food or grocery, use category "other" and confidence below 0.4.
                """;

        Map<String, Object> inlineData = Map.of(
                "mime_type", mimeType != null && !mimeType.isBlank() ? mimeType : "image/jpeg",
                "data", base64Image
        );
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        userContent.put("parts", List.of(
                Map.of("text", prompt),
                Map.of("inline_data", inlineData)
        ));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1);
        generationConfig.put("maxOutputTokens", 256);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(userContent));
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response =
                restTemplate.postForEntity(url, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                if (content != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return String.valueOf(parts.get(0).get("text"));
                    }
                }
            }
        }
        throw AppException.badRequest("Gemini khong tra ve ket qua nhan dien");
    }

    private String extractJson(String text) {
        if (text == null || text.isBlank()) {
            throw AppException.badRequest("Gemini tra ve rong");
        }
        String cleaned = text.replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw AppException.badRequest("Gemini khong tra ve JSON hop le");
        }
        return cleaned.substring(start, end + 1);
    }

    // ── Mock data thông minh (fallback) ─────────────────────────────────────
    private String getMockReply(String userPrompt) {
        String q = userPrompt.toLowerCase().trim();

        // Chào hỏi
        if (q.contains("chào") || q.contains("hello") || q.contains("hi") || q.contains("xin chào")) {
            return "Chào bạn! 👋 Mình là VuiVe Bot của Vựa Vui Vẻ.\n\nMình có thể giúp bạn:\n• 🛒 Tìm & tư vấn sản phẩm tươi ngon\n• 🍳 Gợi ý món ăn và cách nấu\n• 📦 Tra cứu đơn hàng & giao hàng\n• 🎁 Thông tin khuyến mãi\n\nBạn cần hỗ trợ gì ạ?";
        }

        // Rau củ quả
        if (q.contains("rau") || q.contains("củ") || q.contains("cải") || q.contains("bắp cải") || q.contains("cà") || q.contains("su hào") || q.contains("bắp") || q.contains("ngô") || q.contains("khoai")) {
            return "🥦 **Rau củ quả tại Vựa Vui Vẻ**\n\nBên mình có sẵn:\n• Rau muống, rau cải, mồng tơi — tươi hái mỗi sáng\n• Cà rốt, khoai tây, bắp cải VietGAP\n• Dưa chuột, cà chua, ớt chuông nhập mới hàng ngày\n\n💚 Tất cả 100% đạt chuẩn VietGAP, không thuốc trừ sâu.\nBạn muốn đặt mua loại rau nào ạ?";
        }

        // Trái cây
        if (q.contains("trái cây") || q.contains("hoa quả") || q.contains("xoài") || q.contains("dưa") || q.contains("chuối") || q.contains("cam") || q.contains("táo") || q.contains("nho") || q.contains("bưởi") || q.contains("ổi")) {
            return "🍎 **Trái cây tươi Vựa Vui Vẻ**\n\nĐang có sẵn hôm nay:\n• 🥭 Xoài cát Hòa Lộc — ngọt thơm, 45.000đ/kg\n• 🍌 Chuối già Nam Mỹ — 25.000đ/nải\n• 🍊 Cam sành Vĩnh Long — 35.000đ/kg\n• 🍇 Nho đỏ Ninh Thuận — 85.000đ/kg\n\n🎁 Dùng mã **VUAVUIVE** giảm ngay 15%!\nBạn muốn đặt loại nào?";
        }

        // Thịt
        if (q.contains("thịt") || q.contains("heo") || q.contains("lợn") || q.contains("bò") || q.contains("gà") || q.contains("vịt") || q.contains("giò") || q.contains("sườn")) {
            return "🥩 **Thịt tươi Vựa Vui Vẻ**\n\nThịt nhập từ trang trại uy tín, giết mổ đúng chuẩn VSATTP:\n• 🐷 Thịt heo ba chỉ — 130.000đ/kg\n• 🐄 Thịt bò thăn — 250.000đ/kg\n• 🐔 Gà ta thả vườn — 120.000đ/con (1–1.5kg)\n• 🦆 Vịt xiêm — 95.000đ/kg\n\nTất cả bảo quản lạnh, giao trong ngày.\nBạn muốn tư vấn cách chế biến không? 🍳";
        }

        // Hải sản / cá
        if (q.contains("cá") || q.contains("tôm") || q.contains("mực") || q.contains("hải sản") || q.contains("cua") || q.contains("ghẹ") || q.contains("sò") || q.contains("nghêu") || q.contains("ốc")) {
            return "🦐 **Hải sản tươi Vựa Vui Vẻ**\n\nNhập trực tiếp từ cảng cá mỗi sáng:\n• 🦐 Tôm sú tươi — 220.000đ/kg\n• 🐟 Cá thu Phú Quốc — 150.000đ/kg\n• 🦑 Mực ống — 180.000đ/kg\n• 🦀 Cua biển — 350.000đ/kg\n\n❄️ Giao hàng bằng thùng đá lạnh để giữ tươi ngon.\nBạn cần đặt loại hải sản nào ạ?";
        }

        // Nấu ăn / món ăn / công thức
        if (q.contains("nấu") || q.contains("món") || q.contains("công thức") || q.contains("thực đơn") || q.contains("làm") || q.contains("chế biến") || q.contains("recipe")) {
            return "🍳 **Gợi ý món ăn từ VuiVe Bot**\n\nMột số món ngon dễ nấu:\n• **Bò xào cần tỏi**: Thịt bò thăn + cần tỏi Vựa Vui Vẻ, xào lửa to 5 phút là xong!\n• **Canh chua cá lóc**: Cá lóc tươi + cà chua + thơm + me — thanh mát ngày hè\n• **Tôm hấp bia sả**: Tôm sú + bia + sả — hấp 8 phút, chấm muối tiêu chanh 🤤\n• **Salad trộn**: Rau xanh VietGAP + dầu ô liu + chanh — healthy và ngon\n\nBạn muốn mình hướng dẫn chi tiết món nào không?";
        }

        // Giá / khuyến mãi / voucher
        if (q.contains("giá") || q.contains("bao nhiêu") || q.contains("tiền") || q.contains("khuyến mãi") || q.contains("voucher") || q.contains("mã") || q.contains("giảm") || q.contains("sale") || q.contains("freeship")) {
            return "🎁 **Khuyến mãi đang có tại Vựa Vui Vẻ**\n\n• 🏷️ Mã **VUAVUIVE** — Giảm **15%** cho mọi đơn hàng\n• 🚚 Mã **FREESHIP24** — Miễn phí vận chuyển (đơn từ 150k)\n• ⭐ Mua hàng tích điểm đổi quà — 1.000đ = 1 điểm\n\nÁp dụng mã tại bước thanh toán trong giỏ hàng nhé!\nBạn cần tư vấn thêm về sản phẩm gì không?";
        }

        // Đơn hàng / giao hàng / tracking
        if (q.contains("đơn") || q.contains("giao hàng") || q.contains("ship") || q.contains("vận chuyển") || q.contains("theo dõi") || q.contains("order") || q.contains("tracking") || q.contains("bao lâu")) {
            return "📦 **Thông tin giao hàng Vựa Vui Vẻ**\n\n• ⏱️ Thời gian giao: **2–4 giờ** sau khi xác nhận đơn\n• 🗺️ Khu vực giao: Nội thành và vùng lân cận\n• 💰 Phí ship: 20.000đ (miễn phí với mã FREESHIP24)\n• 🔍 Tra cứu đơn: Vào tab **Đơn hàng** → chọn đơn → xem chi tiết\n\nNếu bạn cần tra cứu đơn cụ thể, hãy cung cấp mã đơn để mình hỗ trợ nhé!";
        }

        // Thanh toán
        if (q.contains("thanh toán") || q.contains("payment") || q.contains("momo") || q.contains("zalopay") || q.contains("cod") || q.contains("tiền mặt") || q.contains("chuyển khoản")) {
            return "💳 **Phương thức thanh toán tại Vựa Vui Vẻ**\n\nBên mình hỗ trợ:\n• 💵 **COD** — Thanh toán tiền mặt khi nhận hàng\n• 📱 **MoMo** — Ví điện tử, nhanh và tiện\n• 🏦 **ZaloPay** — Ví điện tử ZaloPay\n\nTất cả đều an toàn và bảo mật. Bạn muốn thanh toán theo hình thức nào?";
        }

        // Sản phẩm hữu cơ / sạch / VietGAP
        if (q.contains("hữu cơ") || q.contains("organic") || q.contains("sạch") || q.contains("vietgap") || q.contains("an toàn") || q.contains("không thuốc")) {
            return "🌿 **Cam kết chất lượng Vựa Vui Vẻ**\n\n• ✅ 100% rau củ đạt chứng nhận **VietGAP**\n• ✅ Không dùng thuốc trừ sâu hóa học\n• ✅ Thịt từ trang trại đạt chuẩn VSATTP\n• ✅ Hải sản nhập mới từ cảng mỗi sáng\n• ✅ Có mã QR truy xuất nguồn gốc từng sản phẩm\n\nBạn yên tâm mua sắm tại Vựa Vui Vẻ nhé! 💚";
        }

        // Tài khoản / đăng nhập / đăng ký
        if (q.contains("tài khoản") || q.contains("đăng nhập") || q.contains("đăng ký") || q.contains("mật khẩu") || q.contains("login") || q.contains("register") || q.contains("quên mật khẩu")) {
            return "👤 **Hỗ trợ tài khoản**\n\nBạn đang gặp vấn đề gì với tài khoản?\n• **Quên mật khẩu**: Chọn 'Quên mật khẩu' ở màn hình đăng nhập → nhập email → kiểm tra hộp thư\n• **Chưa có tài khoản**: Chọn 'Đăng ký' → điền thông tin → xác nhận OTP\n• **Lỗi đăng nhập**: Kiểm tra email/mật khẩu hoặc thử đổi mật khẩu mới\n\nNếu vẫn chưa giải quyết được, hãy liên hệ hotline hỗ trợ nhé!";
        }

        // Điểm thưởng / loyalty
        if (q.contains("điểm") || q.contains("tích điểm") || q.contains("điểm thưởng") || q.contains("loyalty") || q.contains("reward")) {
            return "⭐ **Chương trình tích điểm Vựa Vui Vẻ**\n\n• Mỗi **1.000đ** chi tiêu = **1 điểm** tích lũy\n• **100 điểm** = Giảm 10.000đ cho đơn hàng tiếp theo\n• Điểm không có thời hạn sử dụng\n• Xem điểm tại: Tài khoản → Điểm thưởng\n\nBạn đã có bao nhiêu điểm rồi? Cần hỗ trợ đổi điểm không?";
        }

        // Default response thông minh
        return "💡 Mình là VuiVe Bot của Vựa Vui Vẻ!\n\nMình có thể tư vấn về:\n• 🥬 **Rau củ quả** — VietGAP, tươi ngon\n• 🥩 **Thịt & Hải sản** — nhập mới mỗi ngày\n• 🍳 **Món ăn** — gợi ý công thức nấu\n• 📦 **Đơn hàng** — tra cứu, giao hàng\n• 🎁 **Khuyến mãi** — mã VUAVUIVE giảm 15%\n\nBạn hỏi chi tiết hơn để mình hỗ trợ tốt nhất nhé! 🌿";
    }
}
