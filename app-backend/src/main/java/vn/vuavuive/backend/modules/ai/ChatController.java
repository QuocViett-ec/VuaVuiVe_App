package vn.vuavuive.backend.modules.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public record ChatResponse(
            String reply
    ) {}

    @Operation(summary = "Gửi tin nhắn hỏi trợ lý ảo Gemini AI")
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String reply = geminiService.chatWithBot(request.message());
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}
