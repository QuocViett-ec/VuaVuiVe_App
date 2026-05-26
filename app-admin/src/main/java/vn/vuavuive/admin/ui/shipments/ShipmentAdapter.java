package vn.vuavuive.admin.ui.shipments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.vuavuive.admin.databinding.ItemShipmentBinding;
import vn.vuavuive.shared.data.dto.Shipment;
import vn.vuavuive.shared.util.CurrencyFormatter;

public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.ShipmentViewHolder> {

    private List<Shipment> shipments;
    private final OnShipmentClickListener listener;

    public interface OnShipmentClickListener {
        void onShipmentClick(Shipment shipment);
    }

    public ShipmentAdapter(List<Shipment> shipments, OnShipmentClickListener listener) {
        this.shipments = shipments;
        this.listener = listener;
    }

    public void updateData(List<Shipment> newShipments) {
        this.shipments = newShipments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShipmentBinding binding = ItemShipmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ShipmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShipmentViewHolder holder, int position) {
        holder.bind(shipments.get(position));
    }

    @Override
    public int getItemCount() {
        return shipments.size();
    }

    class ShipmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemShipmentBinding binding;

        public ShipmentViewHolder(ItemShipmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Shipment shipment) {
            binding.tvTrackingNumber.setText(shipment.getTrackingNumber());
            binding.tvOrderIdVal.setText("Đơn: " + shipment.getOrderId());
            
            String carrier = "Nội bộ Vựa Vui Vẻ";
            if ("external".equalsIgnoreCase(shipment.getCarrier())) {
                carrier = "Giao Hàng Nhanh (GHN)";
            }
            binding.tvCarrierLabel.setText("Đơn vị: " + carrier);
            
            binding.tvShipmentFee.setText("Phí ship: " + CurrencyFormatter.formatVnd(shipment.getShippingFee()));

            String eta = shipment.getEta() != null ? shipment.getEta().split("T")[0] : "";
            binding.tvShipmentEta.setText("Giao dự kiến: " + eta);

            // Colorful status chips
            setupStatusChip(shipment.getCurrentStatus());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShipmentClick(shipment);
                } else {
                    Intent intent = new Intent(itemView.getContext(), ShipmentDetailActivity.class);
                    intent.putExtra("SHIPMENT_ID", shipment.getId());
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        private void setupStatusChip(String status) {
            int bgTint, textColor;
            String statusText = status != null ? status.toUpperCase() : "PENDING";

            switch (statusText) {
                case "PENDING":
                    bgTint = Color.parseColor("#33FF9800"); // Warning orange translucent
                    textColor = Color.parseColor("#FF9800");
                    statusText = "CHỜ LẤY HÀNG";
                    break;
                case "PICKED_UP":
                case "PROCESSING":
                    bgTint = Color.parseColor("#3300BCD4"); // Info Cyan translucent
                    textColor = Color.parseColor("#00BCD4");
                    statusText = "ĐÃ LẤY HÀNG";
                    break;
                case "SHIPPED":
                case "SHIPPING":
                    bgTint = Color.parseColor("#332196F3"); // Primary Blue translucent
                    textColor = Color.parseColor("#2196F3");
                    statusText = "ĐANG GIAO HÀNG";
                    break;
                case "DELIVERED":
                    bgTint = Color.parseColor("#334CAF50"); // Success Green translucent
                    textColor = Color.parseColor("#4CAF50");
                    statusText = "ĐÃ GIAO THÀNH CÔNG";
                    break;
                case "CANCELLED":
                case "FAILED":
                    bgTint = Color.parseColor("#33F44336"); // Crimson red translucent
                    textColor = Color.parseColor("#F44336");
                    statusText = "GIAO THẤT BẠI";
                    break;
                default:
                    bgTint = Color.parseColor("#33777777");
                    textColor = Color.parseColor("#777777");
                    break;
            }

            binding.tvShipmentStatus.setText(statusText);
            binding.tvShipmentStatus.setTextColor(textColor);
            binding.tvShipmentStatus.setBackgroundTintList(ColorStateList.valueOf(bgTint));
        }
    }
}
