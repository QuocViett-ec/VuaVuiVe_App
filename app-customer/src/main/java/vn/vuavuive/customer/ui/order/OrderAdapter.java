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
import java.util.ArrayList;
import java.util.List;
import vn.vuavuive.customer.R;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.util.CurrencyFormatter;

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

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvTotal, tvDate, tvItemsSummary, tvItemCount;
        ImageView ivFirstItem;

        OrderViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvItemsSummary = itemView.findViewById(R.id.tv_items_summary);
            tvItemCount = itemView.findViewById(R.id.tv_item_count);
            ivFirstItem = itemView.findViewById(R.id.iv_first_item);
        }

        void bind(Order order) {
            String orderId = order.getOrderId() != null ? order.getOrderId() : order.getId();
            tvOrderId.setText("#" + orderId);

            tvStatus.setText(getStatusLabel(order));
            int statusColor = order.getReturnRequest() != null
                    ? R.color.status_return : getStatusColor(order.getStatus());
            tvStatus.setTextColor(context.getResources().getColor(statusColor, null));

            tvTotal.setText(CurrencyFormatter.format(order.getFinalAmount()));

            String dateStr = order.getCreatedAt() != null && order.getCreatedAt().length() >= 10
                    ? order.getCreatedAt().substring(0, 10) : "";
            tvDate.setText(dateStr);

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                String firstName = order.getItems().get(0).getProductName();
                int extraCount = order.getItems().size() - 1;
                tvItemsSummary.setText(firstName
                        + (extraCount > 0 ? " và " + extraCount + " sản phẩm khác" : ""));
                if (tvItemCount != null) {
                    tvItemCount.setText(order.getItems().size() + " sản phẩm");
                }
                if (ivFirstItem != null) {
                    Glide.with(context)
                            .load(order.getItems().get(0).getImageUrl())
                            .placeholder(R.drawable.ic_image)
                            .error(R.drawable.ic_image)
                            .centerCrop()
                            .into(ivFirstItem);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOrderClick(order);
            });
        }

        private String getStatusLabel(Order order) {
            if (order.getReturnRequest() != null && order.getReturnRequest().getStatus() != null) {
                switch (order.getReturnRequest().getStatus().toUpperCase()) {
                    case "PENDING": return "Trả hàng: Chờ duyệt";
                    case "APPROVED": return "Trả hàng: Đã duyệt";
                    case "REJECTED": return "Trả hàng: Từ chối";
                    case "RECEIVED": return "Đã nhận hàng trả";
                    default: return "Trả hàng";
                }
            }
            String status = order.getStatus();
            if (status == null) return "-";
            switch (status.toLowerCase()) {
                case "pending_payment":
                    return "Chờ thanh toán";
                case "pending_approval":
                    return "Chờ admin duyệt";
                case "confirmed":
                    return "Đã xác nhận";
                case "in_transit":
                    return "Đang giao";
                case "delivered":
                    return "Đã giao";
                case "cancelled":
                    return "Đã hủy";
                case "failed":
                    return "Giao thất bại";
                case "returned":
                    return "Đã trả";
                default:
                    return status;
            }
        }

        private int getStatusColor(String status) {
            if (status == null) return R.color.text_secondary;
            switch (status.toLowerCase()) {
                case "pending_payment":
                case "pending_approval":
                    return R.color.status_pending;
                case "confirmed":
                    return R.color.status_confirmed;
                case "in_transit":
                    return R.color.status_shipping;
                case "delivered":
                    return R.color.status_delivered;
                case "cancelled":
                case "failed":
                    return R.color.status_cancelled;
                default:
                    return R.color.status_return;
            }
        }
    }
}
