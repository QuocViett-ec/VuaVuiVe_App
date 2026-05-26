package vn.vuavuive.admin.ui.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.databinding.ItemChatBubbleBotBinding;
import vn.vuavuive.admin.databinding.ItemChatBubbleUserBinding;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT  = 1;

    public static class ChatMessage {
        public String text;
        public boolean isBot;
        public String timestamp;
        public List<String> quickReplies;

        public ChatMessage(String text, boolean isBot, String timestamp) {
            this.text = text;
            this.isBot = isBot;
            this.timestamp = timestamp;
        }

        public ChatMessage(String text, boolean isBot, String timestamp, List<String> quickReplies) {
            this.text = text;
            this.isBot = isBot;
            this.timestamp = timestamp;
            this.quickReplies = quickReplies;
        }
    }

    private final List<ChatMessage> messages;
    private final OnQuickReplyClickListener quickReplyListener;

    public interface OnQuickReplyClickListener {
        void onQuickReplyClick(String replyText);
    }

    public ChatAdapter(List<ChatMessage> messages, OnQuickReplyClickListener quickReplyListener) {
        this.messages = messages;
        this.quickReplyListener = quickReplyListener;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isBot ? TYPE_BOT : TYPE_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_BOT) {
            ItemChatBubbleBotBinding binding = ItemChatBubbleBotBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new BotViewHolder(binding);
        } else {
            ItemChatBubbleUserBinding binding = ItemChatBubbleUserBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new UserViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (getItemViewType(position) == TYPE_BOT) {
            ((BotViewHolder) holder).bind(message);
        } else {
            ((UserViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ── User bubble ─────────────────────────────────────────────────────────
    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatBubbleUserBinding binding;

        UserViewHolder(ItemChatBubbleUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message) {
            // XML ids: tv_chat_message → tvChatMessage, tv_chat_time → tvChatTime
            binding.tvChatMessage.setText(message.text);
            binding.tvChatTime.setText(message.timestamp);
        }
    }

    // ── Bot bubble ──────────────────────────────────────────────────────────
    class BotViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatBubbleBotBinding binding;

        BotViewHolder(ItemChatBubbleBotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message) {
            // XML ids: tv_chat_message → tvChatMessage, tv_chat_time → tvChatTime
            binding.tvChatMessage.setText(message.text);
            binding.tvChatTime.setText(message.timestamp);

            // Quick-reply chips (chipGroupOptions is added to the XML below)
            if (binding.chipGroupOptions != null) {
                binding.chipGroupOptions.removeAllViews();
                if (message.quickReplies != null && !message.quickReplies.isEmpty()) {
                    binding.chipGroupOptions.setVisibility(View.VISIBLE);
                    for (String option : message.quickReplies) {
                        Chip chip = new Chip(itemView.getContext());
                        chip.setText(option);
                        chip.setChipBackgroundColorResource(R.color.surface_variant);
                        chip.setTextColor(itemView.getContext().getColor(R.color.primary));
                        // Use correct Material Chip stroke API
                        chip.setChipStrokeColorResource(R.color.primary_translucent);
                        chip.setChipStrokeWidth(2f);
                        chip.setOnClickListener(v -> {
                            if (quickReplyListener != null) quickReplyListener.onQuickReplyClick(option);
                        });
                        binding.chipGroupOptions.addView(chip);
                    }
                } else {
                    binding.chipGroupOptions.setVisibility(View.GONE);
                }
            }
        }
    }
}
