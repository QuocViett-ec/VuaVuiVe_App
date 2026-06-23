package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.ChatbotApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@HiltViewModel
public class ChatViewModel extends ViewModel {

    public static class ChatResponse {
        public boolean success;
        public String reply;
    }

    private final ChatbotApi chatbotApi;

    @Inject
    public ChatViewModel(ChatbotApi chatbotApi) {
        this.chatbotApi = chatbotApi;
    }

    public LiveData<ChatResponse> sendMessage(String message) {
        MutableLiveData<ChatResponse> result = new MutableLiveData<>();
        Map<String, String> body = new HashMap<>();
        body.put("message", message);

        chatbotApi.sendMessage(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {
                ChatResponse chatResponse = new ChatResponse();
                if (response.isSuccessful() && response.body() != null) {
                    // Backend bọc tất cả response trong ApiResponse: {success, data: {reply}}
                    // Cần unwrap data trước khi lấy reply
                    Object replyObj = null;
                    Object dataRaw = response.body().get("data");
                    if (dataRaw instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataRaw;
                        replyObj = data.get("reply");
                    }
                    // Fallback: thử đọc trực tiếp nếu không có wrapper
                    if (replyObj == null) {
                        replyObj = response.body().get("reply");
                    }
                    chatResponse.success = true;
                    chatResponse.reply   = replyObj != null ? replyObj.toString()
                            : "Xin lỗi, tôi không hiểu câu hỏi đó.";
                } else {
                    chatResponse.success = false;
                    chatResponse.reply   = null;
                }
                result.postValue(chatResponse);
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                result.postValue(null);
            }
        });
        return result;
    }
}
