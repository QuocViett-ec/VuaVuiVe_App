package vn.vuavuive.customer.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import vn.vuavuive.customer.R;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT  = 1;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String text);
    }

    private final Context context;
    private final List<ChatMessage> messages = new ArrayList<>();
    private OnSuggestionClickListener suggestionListener;

    public ChatAdapter(Context context) {
        this.context = context;
    }

    public void setSuggestionListener(OnSuggestionClickListener listener) {
        this.suggestionListener = listener;
    }

    public void addMessage(ChatMessage message) {
        // Ẩn chips của tin bot trước đó (nếu có) khi user gửi tin mới
        if (message.isUser()) {
            hidePreviousSuggestions();
        }
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    /** Ẩn chips của tất cả tin bot cũ (chỉ giữ chips của tin cuối cùng) */
    private void hidePreviousSuggestions() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage m = messages.get(i);
            if (!m.isUser() && m.getSuggestions() != null) {
                // Tạo bản sao không có suggestions
                messages.set(i, new ChatMessage(m.getContent(), false, null));
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? TYPE_USER : TYPE_BOT;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
            return new UserVH(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.item_chat_bot, parent, false);
            return new BotVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof UserVH) {
            ((UserVH) holder).tvMessage.setText(msg.getContent());
        } else if (holder instanceof BotVH) {
            BotVH botVH = (BotVH) holder;
            botVH.tvMessage.setText(msg.getContent());

            // Hiển thị chips gợi ý
            botVH.chipGroup.removeAllViews();
            List<String> suggestions = msg.getSuggestions();
            if (suggestions != null && !suggestions.isEmpty()) {
                botVH.chipGroup.setVisibility(View.VISIBLE);
                for (String suggestion : suggestions) {
                    Chip chip = new Chip(context);
                    chip.setText(suggestion);
                    chip.setChipBackgroundColorResource(R.color.secondary_light);
                    chip.setTextColor(context.getColor(R.color.primary));
                    chip.setChipStrokeColorResource(R.color.primary);
                    chip.setChipStrokeWidth(1.5f);
                    chip.setClickable(true);
                    chip.setOnClickListener(v -> {
                        if (suggestionListener != null) {
                            suggestionListener.onSuggestionClick(suggestion);
                        }
                    });
                    botVH.chipGroup.addView(chip);
                }
            } else {
                botVH.chipGroup.setVisibility(View.GONE);
            }
        }
    }

    @Override public int getItemCount() { return messages.size(); }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView tvMessage;
        UserVH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
        }
    }

    static class BotVH extends RecyclerView.ViewHolder {
        TextView tvMessage;
        ChipGroup chipGroup;
        BotVH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            chipGroup = v.findViewById(R.id.chip_group_suggestions);
        }
    }
}
