package vn.vuavuive.admin.ui.orders;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.databinding.ItemOrderBinding;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.util.CurrencyFormatter;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private final OnOrderClickListener listener;
    private boolean multiSelectMode = false;
    private final Set<String> selectedOrderIds = new HashSet<>();

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onOrderSelectionChanged(int selectedCount);
    }

    public OrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    public void updateData(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    public void setMultiSelectMode(boolean enabled) {
        this.multiSelectMode = enabled;
        if (!enabled) {
            selectedOrderIds.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    public Set<String> getSelectedOrderIds() {
        return selectedOrderIds;
    }

    public void toggleSelection(String orderId) {
        if (selectedOrderIds.contains(orderId)) {
            selectedOrderIds.remove(orderId);
        } else {
            selectedOrderIds.add(orderId);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onOrderSelectionChanged(selectedOrderIds.size());
        }
    }

    public void selectAll(boolean select) {
        selectedOrderIds.clear();
        if (select) {
            for (Order o : orders) {
                if (o.getId() != null) selectedOrderIds.add(o.getId());
            }
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onOrderSelectionChanged(selectedOrderIds.size());
        }
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
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
        private final ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            binding.tvOrderId.setText(order.getOrderId() != null ? order.getOrderId() : order.getId());
            binding.tvOrderDate.setText(order.getCreatedAt() != null ? order.getCreatedAt().replace("T", " ").replace("Z", "") : "");
            
            if (order.getRecipientName() != null || order.getRecipientPhone() != null) {
                binding.tvCustomerName.setText(order.getRecipientName() != null ? order.getRecipientName() : "Khách hàng VVV");
                binding.tvCustomerPhone.setText("SĐT: " + (order.getRecipientPhone() != null ? order.getRecipientPhone() : "N/A"));
            } else {
                binding.tvCustomerName.setText("Khách hàng VVV");
                binding.tvCustomerPhone.setText("SĐT: N/A");
            }

            binding.tvOrderAmount.setText(CurrencyFormatter.formatVnd(order.getFinalAmount()));

            String method = "COD";
            if (order.getPayment() != null && order.getPayment().getMethod() != null) {
                method = order.getPayment().getMethod().toUpperCase();
                if (order.getPayment().getStatus() != null) {
                    method += " " + order.getPayment().getStatus().toUpperCase();
                }
            }
            binding.tvPaymentBadge.setText(method);

            // Configure checkbox for bulk selection
            if (multiSelectMode) {
                binding.cbSelect.setVisibility(View.VISIBLE);
                binding.cbSelect.setChecked(selectedOrderIds.contains(order.getId()));
                binding.cbSelect.setOnClickListener(v -> toggleSelection(order.getId()));
            } else {
                binding.cbSelect.setVisibility(View.GONE);
            }

            // Set Status Badge colors & translation
            setupStatusBadge(order.getStatus());

            itemView.setOnClickListener(v -> {
                if (multiSelectMode) {
                    toggleSelection(order.getId());
                } else if (listener != null) {
                    listener.onOrderClick(order);
                } else {
                    // Default open details activity
                    Intent intent = new Intent(itemView.getContext(), AdminOrderDetailActivity.class);
                    intent.putExtra("ORDER_ID", order.getId());
                    itemView.getContext().startActivity(intent);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (!multiSelectMode) {
                    setMultiSelectMode(true);
                    toggleSelection(order.getId());
                    return true;
                }
                return false;
            });
        }

        private void setupStatusBadge(String status) {
            int bgTint, textColor;
            String statusText;

            switch (status != null ? status.toLowerCase() : "") {
                case "pending":
                    bgTint = Color.parseColor("#33FF9800"); // Warning translucent
                    textColor = Color.parseColor("#FF9800");
                    statusText = "CHỜ DUYỆT";
                    break;
                case "confirmed":
                    bgTint = Color.parseColor("#3300BCD4"); // Info translucent (Cyan)
                    textColor = Color.parseColor("#00BCD4");
                    statusText = "ĐÃ XÁC NHẬN";
                    break;
                case "processing":
                    bgTint = Color.parseColor("#332196F3"); // Primary translucent
                    textColor = Color.parseColor("#2196F3");
                    statusText = "ĐANG XỬ LÝ";
                    break;
                case "shipped":
                case "shipping":
                    bgTint = Color.parseColor("#339C27B0"); // Purple translucent
                    textColor = Color.parseColor("#9C27B0");
                    statusText = "ĐANG GIAO";
                    break;
                case "delivered":
                    bgTint = Color.parseColor("#334CAF50"); // Success translucent
                    textColor = Color.parseColor("#4CAF50");
                    statusText = "ĐÃ GIAO";
                    break;
                case "cancelled":
                    bgTint = Color.parseColor("#33F44336"); // Error translucent
                    textColor = Color.parseColor("#F44336");
                    statusText = "ĐÃ HỦY";
                    break;
                case "return_requested":
                    bgTint = Color.parseColor("#33E91E63"); // Pink translucent
                    textColor = Color.parseColor("#E91E63");
                    statusText = "Y/C TRẢ HÀNG";
                    break;
                case "return_approved":
                    bgTint = Color.parseColor("#33009688"); // Teal translucent
                    textColor = Color.parseColor("#009688");
                    statusText = "ĐÃ DUYỆT TRẢ";
                    break;
                case "return_rejected":
                    bgTint = Color.parseColor("#33607D8B"); // Gray translucent
                    textColor = Color.parseColor("#607D8B");
                    statusText = "TỪ CHỐI TRẢ";
                    break;
                default:
                    bgTint = Color.parseColor("#33777777");
                    textColor = Color.parseColor("#777777");
                    statusText = (status != null ? status.toUpperCase() : "UNKNOWN");
                    break;
            }

            binding.tvStatusBadge.setText(statusText);
            binding.tvStatusBadge.setTextColor(textColor);
            binding.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(bgTint));
        }
    }
}
