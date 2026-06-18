package vn.vuavuive.customer.ui.order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import vn.vuavuive.customer.R;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener { void onOrderClick(Order order); }

    private final Context context;
    private final OnOrderClickListener listener;
    private List<Order> orders = new ArrayList<>();

    public OrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override public int getItemCount() { return orders.size(); }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvTotal, tvDate, tvItemsSummary, tvItemCount;
        ImageView ivFirstItem;

        OrderViewHolder(View itemView) {
            super(itemView);
            tvOrderId      = itemView.findViewById(R.id.tv_order_id);
            tvStatus       = itemView.findViewById(R.id.tv_status);
            tvTotal        = itemView.findViewById(R.id.tv_total);
            tvDate         = itemView.findViewById(R.id.tv_date);
            tvItemsSummary = itemView.findViewById(R.id.tv_items_summary);
            tvItemCount    = itemView.findViewById(R.id.tv_item_count);
            ivFirstItem    = itemView.findViewById(R.id.iv_first_item);
        }

        void bind(Order order) {
            // Order ID
            String orderId = order.getOrderId() != null ? order.getOrderId() : order.getId();
            tvOrderId.setText("#" + orderId);

            // Status
            tvStatus.setText(getStatusLabel(order.getStatus()));
            int statusColor = getStatusColor(order.getStatus());
            tvStatus.setTextColor(context.getResources().getColor(statusColor, null));

            // Total
            tvTotal.setText(CurrencyFormatter.format(order.getFinalAmount()));

            // Date
            String dateStr = order.getCreatedAt() != null && order.getCreatedAt().length() >= 10
                    ? order.getCreatedAt().substring(0, 10) : "";
            tvDate.setText(dateStr);

            // Items summary
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                String firstName = order.getItems().get(0).getProductName();
                int extraCount = order.getItems().size() - 1;
                tvItemsSummary.setText(firstName
                        + (extraCount > 0 ? " và " + extraCount + " sản phẩm khác" : ""));
                if (tvItemCount != null) {
                    tvItemCount.setText(order.getItems().size() + " sản phẩm");
                }
                // Load first item image
                if (ivFirstItem != null && order.getItems().get(0).getImageUrl() != null) {
                    Glide.with(context)
                            .load(order.getItems().get(0).getImageUrl())
                            .placeholder(R.drawable.ic_image)
                            .centerCrop()
                            .into(ivFirstItem);
                }
            }

            itemView.setOnClickListener(v -> { if (listener != null) listener.onOrderClick(order); });
        }

        private String getStatusLabel(String status) {
            if (status == null) return "—";
            switch (status.toLowerCase()) {
                case "pending":          return "Chờ xác nhận";
                case "confirmed":        return "Đã xác nhận";
                case "shipping":         return "Đang giao";
                case "delivered":        return "Đã giao";
                case "cancelled":        return "Đã hủy";
                case "return_requested": return "Yêu cầu trả";
                case "returned":         return "Đã trả";
                case "refunded":         return "Đã hoàn tiền";
                default:                 return status;
            }
        }

        private int getStatusColor(String status) {
            if (status == null) return R.color.text_secondary;
            switch (status.toLowerCase()) {
                case "pending":          return R.color.status_pending;
                case "confirmed":        return R.color.status_confirmed;
                case "shipping":         return R.color.status_shipping;
                case "delivered":        return R.color.status_delivered;
                case "cancelled":        return R.color.status_cancelled;
                default:                 return R.color.status_return;
            }
        }
    }
}
