package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.ChatbotApi;
import vn.vuavuive.customer.ui.chat.ChatMessage;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HiltViewModel
public class ChatViewModel extends ViewModel {

    public static class ChatResponse {
        public boolean success;
        public String reply;
        public List<ChatMessage.ProductItem> products;
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
                    Map<String, Object> rawBody = response.body();

                    // Unwrap ApiResponse wrapper: { success, data: { reply, products } }
                    Object replyObj  = null;
                    Object productsObj = null;
                    Object dataRaw = rawBody.get("data");
                    if (dataRaw instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataRaw;
                        replyObj    = data.get("reply");
                        productsObj = data.get("products");
                    }
                    // Fallback: direct fields
                    if (replyObj == null) replyObj    = rawBody.get("reply");
                    if (productsObj == null) productsObj = rawBody.get("products");

                    chatResponse.success = true;
                    chatResponse.reply   = replyObj != null ? replyObj.toString()
                            : "Xin lỗi, tôi không hiểu câu hỏi đó.";

                    // Parse products list
                    chatResponse.products = parseProducts(productsObj);
                } else {
                    chatResponse.success  = false;
                    chatResponse.reply    = null;
                    chatResponse.products = null;
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

    @SuppressWarnings("unchecked")
    private List<ChatMessage.ProductItem> parseProducts(Object productsObj) {
        if (!(productsObj instanceof List)) return null;
        List<ChatMessage.ProductItem> items = new ArrayList<>();
        try {
            List<Object> list = (List<Object>) productsObj;
            for (Object o : list) {
                if (!(o instanceof Map)) continue;
                Map<String, Object> m = (Map<String, Object>) o;
                ChatMessage.ProductItem p = new ChatMessage.ProductItem();
                p.id       = getString(m, "id");
                p.name     = getString(m, "name");
                p.unit     = getString(m, "unit");
                p.imageUrl = getString(m, "imageUrl");
                Object priceObj = m.get("price");
                if (priceObj instanceof Number) p.price = ((Number) priceObj).doubleValue();
                if (p.name != null) items.add(p);
            }
        } catch (Exception ignored) {}
        return items.isEmpty() ? null : items;
    }

    private String getString(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }
}
