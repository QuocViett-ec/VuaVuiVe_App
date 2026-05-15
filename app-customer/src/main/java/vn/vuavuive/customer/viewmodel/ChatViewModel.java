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

        chatbotApi.sendMessage(body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {
                ChatResponse chatResponse = new ChatResponse();
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    Object reply = response.body().getData().get("reply");
                    chatResponse.success = true;
                    chatResponse.reply   = reply != null ? reply.toString()
                            : "Xin lỗi, tôi không hiểu câu hỏi đó.";
                } else {
                    chatResponse.success = false;
                    chatResponse.reply   = null;
                }
                result.postValue(chatResponse);
            }
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                result.postValue(null);
            }
        });
        return result;
    }
}
