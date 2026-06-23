package vn.vuavuive.shared.data.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import vn.vuavuive.shared.data.dto.ApiResponse;

public interface ChatbotApi {

    /**
     * Customer chatbot — Gemini AI
     * Body: { message: String, sessionId: String (optional) }
     */
    @POST("api/ai/chat")
    Call<Map<String, Object>> sendMessage(@Body Map<String, String> body);
}
