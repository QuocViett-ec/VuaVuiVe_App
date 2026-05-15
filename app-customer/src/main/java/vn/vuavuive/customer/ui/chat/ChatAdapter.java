package vn.vuavuive.customer.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import vn.vuavuive.customer.R;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT  = 1;

    private final Context context;
    private final List<ChatMessage> messages = new ArrayList<>();

    public ChatAdapter(Context context) {
        this.context = context;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
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
            ((BotVH) holder).tvMessage.setText(msg.getContent());
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
        BotVH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
        }
    }
}
