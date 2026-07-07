package vn.vuavuive.backend.modules.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.vision.VisionSearchResponse;

import java.util.*;

/**
 * GeminiService - Kết nối Google Gemini 2.5 Flash để cung cấp chatbot tư vấn Vựa Vui Vẻ.
 * Tự động tìm sản phẩm liên quan từ Firebase REST API và nhúng vào context trước khi gọi AI.
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
    private final FirebaseRepositoryHelper firebase;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── System Prompt ────────────────────────────────────────────────────────
    private static final String SYSTEM_PROMPT =
            "Bạn là VuiVe Bot - trợ lý ảo thông minh của Vựa Vui Vẻ, một nền tảng thương mại điện tử " +
            "chuyên cung cấp thực phẩm tươi sạch tại Việt Nam.\n\n" +
            "# Thông tin về Vựa Vui Vẻ\n" +
            "- Chuyên cung cấp: rau củ quả VietGAP, thịt tươi sạch, hải sản nhập mới hàng ngày\n" +
            "- Giao hàng trong 2–4 giờ, nội thành và vùng lân cận\n" +
            "- Voucher: VUAVUIVE giảm 15%, FREESHIP24 miễn phí ship (đơn từ 150k)\n" +
            "- Thanh toán: COD, MoMo, ZaloPay\n\n" +
            "# Nhiệm vụ\n" +
            "1. Tư vấn sản phẩm thực phẩm tươi ngon, dinh dưỡng\n" +
            "2. Gợi ý món ăn và công thức nấu ăn phù hợp\n" +
            "3. Hỗ trợ đơn hàng, giao hàng, thanh toán\n" +
            "4. Nếu trong context có danh sách sản phẩm, hãy tự nhiên giới thiệu chúng (tên, giá)\n\n" +
            "# Phong cách\n" +
            "- Thân thiện, vui vẻ, tiếng Việt tự nhiên\n" +
            "- Ngắn gọn, súc tích (tối đa 150 từ)\n" +
            "- Dùng emoji phù hợp\n" +
            "- KHÔNG bịa thông tin sản phẩm nếu không có trong context";

    // ── Model ánh xạ Firebase product ────────────────────────────────────────
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FirebaseProduct {
        public String id;
        public String name;

        @JsonProperty("selling_price")
        public Double sellingPrice;

        @JsonProperty("sellingPrice")
        public Double sellingPrice2;

        public Double price;

        public String unit;

        @JsonProperty("image_url")
        public String imageUrl;

        @JsonProperty("imageUrl")
        public String imageUrl2;

        @JsonProperty("is_active")
        public Boolean isActive;

        @JsonProperty("isActive")
        public Boolean isActive2;

        @JsonProperty("stock_quantity")
        public Integer stockQuantity;

        public double getPrice() {
            if (sellingPrice != null && sellingPrice > 0) return sellingPrice;
            if (sellingPrice2 != null && sellingPrice2 > 0) return sellingPrice2;
            if (price != null && price > 0) return price;
            return 0;
        }

        public String getImage() {
            if (imageUrl != null && !imageUrl.isEmpty()) return imageUrl;
            if (imageUrl2 != null && !imageUrl2.isEmpty()) return imageUrl2;
            return null;
        }

        public boolean isActive() {
            if (isActive != null) return isActive;
            if (isActive2 != null) return isActive2;
            return true; // default active
        }
    }

    /** Dữ liệu sản phẩm trả về cho client */
    public static class ProductInfo {
        public String id;
        public String name;
        public double price;
        public String unit;
        public String imageUrl;

        public String toContextString() {
            return String.format("- %s: %.0fđ/%s", name, price, unit != null ? unit : "kg");
        }
    }

    // ── Main chat method ──────────────────────────────────────────────────────
    public ChatController.ChatResponse chatWithBot(String userPrompt) {
        log.info("VuiVe Bot nhận prompt: {}", userPrompt);

        // 1. Tìm sản phẩm từ Firebase REST
        List<ProductInfo> matchedProducts = searchRelevantProducts(userPrompt);
        log.info("Tìm được {} sản phẩm liên quan", matchedProducts.size());

        // 2. Gọi Gemini AI với context sản phẩm
        try {
            String reply = callGeminiApi(userPrompt, matchedProducts);
            if (reply != null && !reply.isBlank()) {
                log.info("Gemini AI trả lời thành công.");
                return new ChatController.ChatResponse(reply.trim(), matchedProducts);
            }
        } catch (Exception e) {
            log.warn("Gemini API lỗi ({}), chuyển sang mock data.", e.getMessage());
        }

        // 3. Fallback: mock data thông minh
        String mockReply = getMockReply(userPrompt);
        return new ChatController.ChatResponse(mockReply, matchedProducts);
    }

    // ── Tìm sản phẩm dùng FirebaseRepositoryHelper REST ──────────────────────
    @SuppressWarnings("unchecked")
    private List<ProductInfo> searchRelevantProducts(String userPrompt) {
        String query = userPrompt.toLowerCase().trim();
        List<ProductInfo> results = new ArrayList<>();

        try {
            List<FirebaseProduct> allProducts = firebase.getList("products", FirebaseProduct.class);
            log.debug("Loaded {} products from Firebase", allProducts.size());

            for (FirebaseProduct p : allProducts) {
                if (p.name == null) continue;
                if (!p.isActive()) continue;

                if (isProductMatch(query, p)) {
                    ProductInfo info = new ProductInfo();
                    info.id       = p.id;
                    info.name     = p.name;
                    info.price    = p.getPrice();
                    info.unit     = p.unit != null ? p.unit : "kg";
                    info.imageUrl = p.getImage();
                    results.add(info);
                    if (results.size() >= 6) break;
                }
            }
        } catch (Exception e) {
            log.warn("Không thể tìm sản phẩm từ Firebase: {}", e.getMessage());
        }

        return results;
    }

    private boolean isProductMatch(String query, FirebaseProduct p) {
        String nameLower = p.name.toLowerCase();

        // So sánh trực tiếp tên
        if (nameLower.contains(query) || query.contains(nameLower)) return true;

        // So sánh từng từ (độ dài >= 3 ký tự)
        String[] words = query.split("[\\s,]+");
        for (String word : words) {
            if (word.length() >= 3 && nameLower.contains(word)) return true;
        }

        return false;
    }

    // ── Gọi Gemini API ────────────────────────────────────────────────────────
    private String callGeminiApi(String userPrompt, List<ProductInfo> products) {
        String url = String.format(GEMINI_API_URL, model, apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> systemInstruction = new HashMap<>();
        systemInstruction.put("parts", List.of(Map.of("text", SYSTEM_PROMPT)));

        // Tạo prompt user với context sản phẩm
        StringBuilder promptBuilder = new StringBuilder(userPrompt);
        if (!products.isEmpty()) {
            promptBuilder.append("\n\n[Sản phẩm đang có trong cửa hàng liên quan:]\n");
            for (ProductInfo pi : products) {
                promptBuilder.append(pi.toContextString()).append("\n");
            }
        }

        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        userContent.put("parts", List.of(Map.of("text", promptBuilder.toString())));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 500);

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

    // ── Vision API ────────────────────────────────────────────────────────────
    public VisionSearchResponse analyzeProductImage(String base64Image, String mimeType) {
        if (apiKey == null || apiKey.isBlank()) {
            throw AppException.badRequest("Chua cau hinh GEMINI_API_KEY");
        }
        try {
            String text = callGeminiVisionApi(base64Image, mimeType);
            String json = extractJson(text);
            return MAPPER.readValue(json, VisionSearchResponse.class);
        } catch (RestClientResponseException e) {
            log.warn("Gemini Vision HTTP failed: {} {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            int status = e.getRawStatusCode();
            if (status == 429) throw AppException.badRequest("Gemini dang het quota hoac bi gioi han toc do, thu lai sau");
            if (status == 403) throw AppException.badRequest("Gemini API key khong co quyen goi model nay");
            if (status == 404) throw AppException.badRequest("Gemini model khong ton tai, kiem tra GEMINI_MODEL");
            throw AppException.badRequest("Gemini khong xu ly duoc anh");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Gemini Vision failed: {}", e.getMessage());
            throw AppException.badRequest("Khong nhan dien duoc san pham trong anh");
        }
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
        if (text == null || text.isBlank()) throw AppException.badRequest("Gemini tra ve rong");
        String cleaned = text.replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end <= start) throw AppException.badRequest("Gemini khong tra ve JSON hop le");
        return cleaned.substring(start, end + 1);
    }

    // ── Mock fallback ─────────────────────────────────────────────────────────
    private String getMockReply(String userPrompt) {
        String q = userPrompt.toLowerCase().trim();
        if (q.contains("chào") || q.contains("hello") || q.contains("hi")) {
            return "Chào bạn! 👋 Mình là VuiVe Bot của Vựa Vui Vẻ.\nBạn cần tìm sản phẩm gì, gợi ý món ăn hay hỏi đơn hàng ạ?";
        }
        if (q.contains("rau") || q.contains("củ") || q.contains("cải") || q.contains("khoai") || q.contains("bắp") || q.contains("cà")) {
            return "🥦 Bên mình có nhiều rau củ VietGAP tươi ngon!\nNhập mới mỗi sáng, không thuốc trừ sâu.\nBạn muốn tìm loại nào cụ thể?";
        }
        if (q.contains("thịt") || q.contains("heo") || q.contains("bò") || q.contains("gà")) {
            return "🥩 Thịt tươi từ trang trại uy tín!\nGiao hàng bảo quản lạnh, đảm bảo tươi ngon.\nBạn cần loại thịt nào ạ?";
        }
        if (q.contains("cá") || q.contains("tôm") || q.contains("hải sản")) {
            return "🦐 Hải sản tươi nhập trực tiếp từ cảng mỗi sáng!\nGiao bằng thùng đá lạnh.\nBạn cần loại hải sản nào?";
        }
        if (q.contains("khuyến mãi") || q.contains("voucher")) {
            return "🎁 Mã VUAVUIVE giảm 15%, FREESHIP24 miễn phí ship!\nÁp dụng khi thanh toán nhé!";
        }
        if (q.contains("đơn") || q.contains("giao hàng")) {
            return "📦 Giao hàng 2–4 giờ sau xác nhận.\nTra cứu đơn tại tab Đơn hàng nhé!";
        }
        return "💡 Mình là VuiVe Bot!\nMình tư vấn: 🥬 Rau củ • 🥩 Thịt hải sản • 🍳 Món ăn • 📦 Đơn hàng • 🎁 Khuyến mãi\nBạn cần gì ạ?";
    }
}
