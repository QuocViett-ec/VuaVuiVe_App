package vn.vuavuive.customer.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.viewmodel.ChatViewModel;
import java.util.Arrays;
import java.util.List;

@AndroidEntryPoint
public class ChatActivity extends AppCompatActivity {

    private ChatViewModel chatViewModel;
    private ChatAdapter chatAdapter;
    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;

    // Gợi ý mặc định khi bot chào
    private static final List<String> DEFAULT_SUGGESTIONS = Arrays.asList(
            "🥦 Rau củ quả", "🥩 Thịt & hải sản", "🍳 Gợi ý món ăn",
            "🎁 Khuyến mãi", "📦 Đơn hàng"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        initViews();

        // Tin chào + gợi ý nhanh ban đầu
        chatAdapter.addMessage(new ChatMessage(
                "Xin chào! Mình là VuiVe Bot 🤖\nMình có thể giúp bạn tìm sản phẩm, tư vấn món ăn, hỏi về đơn hàng và khuyến mãi. Bạn cần gì nào? 👇",
                false,
                DEFAULT_SUGGESTIONS
        ));
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        rvChat      = findViewById(R.id.rv_chat);
        etMessage   = findViewById(R.id.et_message);
        btnSend     = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id.progress_bar);

        chatAdapter = new ChatAdapter(this);

        // Khi nhấn vào chip gợi ý → điền vào ô nhập và gửi luôn
        chatAdapter.setSuggestionListener(suggestionText -> {
            etMessage.setText(suggestionText);
            sendMessage();
        });

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        rvChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) return;

        chatAdapter.addMessage(new ChatMessage(text, true));
        scrollToBottom();
        etMessage.setText("");
        setBusy(true);

        chatViewModel.sendMessage(text).observe(this, result -> {
            setBusy(false);
            if (result != null && result.reply != null) {
                List<String> suggestions = getSuggestionsForReply(result.reply);
                chatAdapter.addMessage(new ChatMessage(result.reply, false, suggestions));
            } else {
                chatAdapter.addMessage(new ChatMessage(
                        "Xin lỗi, mình không thể kết nối ngay lúc này. Vui lòng thử lại sau. 🙏",
                        false,
                        Arrays.asList("🔄 Thử lại", "📦 Đơn hàng", "🎁 Khuyến mãi")
                ));
            }
            scrollToBottom();
        });
    }

    /**
     * Tự động sinh gợi ý tiếp theo dựa trên nội dung reply của bot.
     */
    private List<String> getSuggestionsForReply(String reply) {
        String r = reply.toLowerCase();
        if (r.contains("rau") || r.contains("vietgap") || r.contains("rau củ")) {
            return Arrays.asList("🥩 Thịt tươi", "🍳 Gợi ý món ăn", "🎁 Khuyến mãi");
        } else if (r.contains("thịt") || r.contains("hải sản") || r.contains("tôm") || r.contains("cá")) {
            return Arrays.asList("🍳 Gợi ý món ăn", "🥦 Rau củ quả", "📦 Đơn hàng");
        } else if (r.contains("món") || r.contains("nấu") || r.contains("công thức")) {
            return Arrays.asList("🛒 Mua nguyên liệu", "🥩 Thịt & hải sản", "🥦 Rau củ quả");
        } else if (r.contains("voucher") || r.contains("khuyến mãi") || r.contains("giảm")) {
            return Arrays.asList("🛒 Mua ngay", "📦 Tra cứu đơn hàng", "🥦 Xem sản phẩm");
        } else if (r.contains("đơn") || r.contains("giao hàng") || r.contains("ship")) {
            return Arrays.asList("🎁 Khuyến mãi", "🥩 Mua thêm", "💳 Thanh toán");
        } else if (r.contains("thanh toán") || r.contains("momo") || r.contains("zalopay")) {
            return Arrays.asList("📦 Đơn hàng của tôi", "🎁 Xem khuyến mãi", "🥦 Mua sắm tiếp");
        } else {
            return Arrays.asList("🥦 Rau củ quả", "🥩 Thịt & hải sản", "🍳 Gợi ý món ăn");
        }
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private void setBusy(boolean busy) {
        btnSend.setEnabled(!busy);
        etMessage.setEnabled(!busy);
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
    }
}
