package vn.vuavuive.shared.data.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import vn.vuavuive.shared.data.dto.ApiResponse;

public interface AdminChatbotApi {

    /**
     * Admin chatbot — Intent-based: tra đơn, thống kê, SP sắp hết...
     * Body: { message: String }
     */
    @POST("api/admin/chatbot")
    Call<ApiResponse<Map<String, Object>>> sendMessage(@Body Map<String, String> body);
}
