package vn.vuavuive.backend.modules.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ChatController - API Chatbot AI tư vấn sản phẩm, món ăn, thực phẩm sạch.
 * Mọi request chat đều được bảo vệ và cần login (Cần gửi JWT).
 */
@Tag(name = "AI Chatbot", description = "API Trợ lý ảo tư vấn nông sản và nấu ăn")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiService geminiService;

    public record ChatRequest(
            @NotBlank(message = "Nội dung tin nhắn không được để trống")
            String message
    ) {}

    /** Response gồm text reply + danh sách sản phẩm liên quan (nếu có) */
    public record ChatResponse(
            String reply,
            List<GeminiService.ProductInfo> products
    ) {}

    @Operation(summary = "Gửi tin nhắn hỏi trợ lý ảo Gemini AI")
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = geminiService.chatWithBot(request.message());
        return ResponseEntity.ok(response);
    }
}
