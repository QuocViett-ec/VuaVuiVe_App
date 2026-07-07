package vn.vuavuive.customer.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.ui.product.ProductDetailActivity;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        if (message.isUser()) {
            hidePreviousSuggestions();
        }
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    private void hidePreviousSuggestions() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage m = messages.get(i);
            if (!m.isUser() && m.getSuggestions() != null) {
                messages.set(i, new ChatMessage(m.getContent(), false, null, m.getProducts()));
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
            BotVH bot = (BotVH) holder;
            bot.tvMessage.setText(msg.getContent());

            // --- Sản phẩm ---
            List<ChatMessage.ProductItem> products = msg.getProducts();
            if (products != null && !products.isEmpty()) {
                bot.scrollProducts.setVisibility(View.VISIBLE);
                bot.containerProducts.removeAllViews();

                LayoutInflater inflater = LayoutInflater.from(context);
                NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

                for (ChatMessage.ProductItem p : products) {
                    View cardView = inflater.inflate(R.layout.item_chat_product, bot.containerProducts, false);

                    ShapeableImageView iv = cardView.findViewById(R.id.iv_product);
                    TextView tvName  = cardView.findViewById(R.id.tv_product_name);
                    TextView tvPrice = cardView.findViewById(R.id.tv_product_price);

                    tvName.setText(p.name);
                    tvPrice.setText(fmt.format((long) p.price) + "đ/" + (p.unit != null ? p.unit : "kg"));

                    if (p.imageUrl != null && !p.imageUrl.isEmpty()) {
                        Glide.with(context)
                                .load(p.imageUrl)
                                .placeholder(R.drawable.ic_image)
                                .centerCrop()
                                .into(iv);
                    }

                    // Click → mở chi tiết sản phẩm
                    if (p.id != null) {
                        cardView.setOnClickListener(v -> {
                            Intent intent = new Intent(context, ProductDetailActivity.class);
                            intent.putExtra("productId", p.id);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });
                    }

                    bot.containerProducts.addView(cardView);
                }
            } else {
                bot.scrollProducts.setVisibility(View.GONE);
                bot.containerProducts.removeAllViews();
            }

            // --- Chips gợi ý ---
            bot.chipGroup.removeAllViews();
            List<String> suggestions = msg.getSuggestions();
            if (suggestions != null && !suggestions.isEmpty()) {
                bot.chipGroup.setVisibility(View.VISIBLE);
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
                    bot.chipGroup.addView(chip);
                }
            } else {
                bot.chipGroup.setVisibility(View.GONE);
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
        HorizontalScrollView scrollProducts;
        LinearLayout containerProducts;
        ChipGroup chipGroup;

        BotVH(View v) {
            super(v);
            tvMessage         = v.findViewById(R.id.tv_message);
            scrollProducts    = v.findViewById(R.id.scroll_products);
            containerProducts = v.findViewById(R.id.container_products);
            chipGroup         = v.findViewById(R.id.chip_group_suggestions);
        }
    }
}
