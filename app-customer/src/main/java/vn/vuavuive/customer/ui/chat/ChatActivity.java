package vn.vuavuive.customer.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.viewmodel.ChatViewModel;

@AndroidEntryPoint
public class ChatActivity extends AppCompatActivity {

    private ChatViewModel chatViewModel;
    private ChatAdapter chatAdapter;
    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        initViews();

        // Welcome message
        chatAdapter.addMessage(new ChatMessage(
                "Xin chào! Tôi là VuiVe Bot 🤖\nTôi có thể giúp bạn tìm sản phẩm, hỏi về đơn hàng và giao hàng. Bạn cần hỗ trợ gì?",
                false));
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        rvChat      = findViewById(R.id.rv_chat);
        etMessage   = findViewById(R.id.et_message);
        btnSend     = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id.progress_bar);

        chatAdapter = new ChatAdapter(this);
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
        rvChat.scrollToPosition(chatAdapter.getItemCount() - 1);
        etMessage.setText("");
        setBusy(true);

        chatViewModel.sendMessage(text).observe(this, result -> {
            setBusy(false);
            if (result != null && result.reply != null) {
                chatAdapter.addMessage(new ChatMessage(result.reply, false));
                rvChat.scrollToPosition(chatAdapter.getItemCount() - 1);
            } else {
                chatAdapter.addMessage(new ChatMessage(
                        "Xin lỗi, tôi không thể kết nối ngay lúc này. Vui lòng thử lại sau.", false));
                rvChat.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    private void setBusy(boolean busy) {
        btnSend.setEnabled(!busy);
        etMessage.setEnabled(!busy);
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
    }
}
