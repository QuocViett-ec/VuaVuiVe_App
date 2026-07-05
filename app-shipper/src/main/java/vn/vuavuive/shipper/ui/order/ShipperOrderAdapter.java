package vn.vuavuive.shipper.ui.order;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.PaymentDetail;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * ShipperOrderAdapter — RecyclerView Adapter hiển thị card đơn hàng cho Shipper.
 * Tích hợp nút Gọi điện (Quick Call) và Chỉ đường (Google Maps Navigate).
 */
public class ShipperOrderAdapter extends ListAdapter<Order, ShipperOrderAdapter.ViewHolder> {

    private final Context context;

    private static final DiffUtil.ItemCallback<Order> DIFF = new DiffUtil.ItemCallback<Order>() {
        @Override
        public boolean areItemsTheSame(@NonNull Order a, @NonNull Order b) {
            return a.getId() != null && a.getId().equals(b.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull Order a, @NonNull Order b) {
            return Objects.equals(a.getStatus(), b.getStatus())
                    && Objects.equals(a.getUpdatedAt(), b.getUpdatedAt())
                    && Objects.equals(a.getRecipientName(), b.getRecipientName())
                    && Objects.equals(a.getRecipientPhone(), b.getRecipientPhone())
                    && Objects.equals(a.getRecipientAddress(), b.getRecipientAddress())
                    && a.getFinalAmount() == b.getFinalAmount();
        }
    };

    public ShipperOrderAdapter(Context context, boolean isHistory) {
        super(DIFF);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_shipper_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCreatedAt, tvStatus, tvCustomerName, tvAddress, tvTotal;
        MaterialButton btnCall, btnNavigate;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId      = itemView.findViewById(R.id.tv_order_id);
            tvCreatedAt    = itemView.findViewById(R.id.tv_created_at);
            tvStatus       = itemView.findViewById(R.id.tv_status);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvAddress      = itemView.findViewById(R.id.tv_address);
            tvTotal        = itemView.findViewById(R.id.tv_total);
            btnCall        = itemView.findViewById(R.id.btn_call);
            btnNavigate    = itemView.findViewById(R.id.btn_navigate);
        }

        void bind(Order order) {
            // Order ID (short)
            String idShort = (order.getId() != null && order.getId().length() >= 8)
                    ? "#" + order.getId().substring(0, 8).toUpperCase() : "#------";
            tvOrderId.setText(idShort);
            tvCreatedAt.setText(formatDate(order.getCreatedAt()));
            bindStatus(order);

            // Recipient info — uses helper methods that support both flat & nested formats
            String name    = order.getRecipientName();
            String phone   = order.getRecipientPhone();
            String address = order.getRecipientAddress();

            tvCustomerName.setText(name != null ? name : "Khách hàng");
            tvAddress.setText(address != null ? address : "Chưa có địa chỉ");

            // ── Quick Call Button ──────────────────────────────────────────
            if (phone != null && !phone.isEmpty()) {
                final String finalPhone = phone.trim();
                btnCall.setVisibility(View.VISIBLE);
                btnCall.setOnClickListener(v ->
                        context.startActivity(new Intent(Intent.ACTION_DIAL,
                                Uri.parse("tel:" + finalPhone))));
            } else {
                btnCall.setVisibility(View.GONE);
            }

            // ── Navigate Button (Google Maps) ──────────────────────────────
            if (address != null && !address.isEmpty()) {
                final String finalAddress = address.trim();
                btnNavigate.setVisibility(View.VISIBLE);
                btnNavigate.setOnClickListener(v -> {
                    Uri mapsUri = Uri.parse("google.navigation:q=" + Uri.encode(finalAddress));
                    Intent mapsIntent = new Intent(Intent.ACTION_VIEW, mapsUri);
                    mapsIntent.setPackage("com.google.android.apps.maps");
                    if (mapsIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(mapsIntent);
                    } else {
                        Uri fallback = Uri.parse("https://www.google.com/maps/search/?api=1&query="
                                + Uri.encode(finalAddress));
                        context.startActivity(new Intent(Intent.ACTION_VIEW, fallback));
                    }
                });
            } else {
                btnNavigate.setVisibility(View.GONE);
            }

            // Total amount
            PaymentDetail pmt = order.getPayment();
            if (pmt != null && "momo".equalsIgnoreCase(pmt.getMethod())) {
                tvTotal.setText(momoBadge(pmt.getStatus()));
            } else if (order.getFinalAmount() > 0) {
                NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                tvTotal.setText("Thu tiền mặt: " + fmt.format(Math.round(order.getFinalAmount())) + " đ");
            } else {
                tvTotal.setText("—");
            }

            // Navigate to detail on card click
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ShipperOrderDetailActivity.class);
                intent.putExtra("order_id", order.getId());
                context.startActivity(intent);
            });
        }

        private void bindStatus(Order order) {
            if (order.getReturnRequest() != null
                    && "APPROVED".equalsIgnoreCase(order.getReturnRequest().getStatus())) {
                tvStatus.setText("Cần nhận hàng trả");
                tvStatus.setBackgroundColor(Color.parseColor("#7B1FA2"));
                return;
            }
            String status = order.getStatus();
            if (status == null) return;
            switch (status.toUpperCase()) {
                case "CONFIRMED":
                    tvStatus.setText("Chờ lấy hàng");
                    tvStatus.setBackgroundColor(Color.parseColor("#FF9800")); break;
                case "IN_TRANSIT":
                    tvStatus.setText("Đang giao");
                    tvStatus.setBackgroundColor(Color.parseColor("#FF6B35")); break;
                case "DELIVERED":
                    tvStatus.setText("Đã giao");
                    tvStatus.setBackgroundColor(Color.parseColor("#1B8A3A")); break;
                case "FAILED":
                    tvStatus.setText("Thất bại");
                    tvStatus.setBackgroundColor(Color.parseColor("#757575")); break;
                case "CANCELLED":
                    tvStatus.setText("Đã hủy");
                    tvStatus.setBackgroundColor(Color.parseColor("#C62828")); break;
                case "RETURNED":
                    tvStatus.setText("Hoàn hàng");
                    tvStatus.setBackgroundColor(Color.parseColor("#7B1FA2")); break;
                default:
                    tvStatus.setText(status);
                    tvStatus.setBackgroundColor(Color.parseColor("#9E9E9E"));
            }
        }

        private String formatDate(String raw) {
            if (raw == null || raw.isEmpty()) return "";
            try {
                String[] parts = raw.split("T");
                String[] d = parts[0].split("-");
                String t = parts.length > 1 ? parts[1].substring(0, 5) : "";
                return d[2] + "/" + d[1] + "/" + d[0] + " · " + t;
            } catch (Exception e) { return raw; }
        }

        private String momoBadge(String status) {
            if ("paid".equalsIgnoreCase(status)) return "Đã thanh toán MoMo";
            if ("failed".equalsIgnoreCase(status)) return "MoMo thất bại";
            return "MoMo chờ thanh toán";
        }
    }
}
