package vn.vuavuive.customer.ui.shipment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import vn.vuavuive.customer.R;
import vn.vuavuive.shared.data.dto.Shipment;
import java.util.ArrayList;
import java.util.List;

public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.ShipmentVH> {

    public interface OnShipmentClickListener {
        void onShipmentClick(Shipment shipment);
    }

    private final Context context;
    private final OnShipmentClickListener listener;
    private List<Shipment> items = new ArrayList<>();

    public ShipmentAdapter(Context context, OnShipmentClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<Shipment> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShipmentVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_shipment, parent, false);
        return new ShipmentVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShipmentVH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ShipmentVH extends RecyclerView.ViewHolder {
        TextView tvTracking, tvStatus, tvCarrier, tvOrderId;

        ShipmentVH(View v) {
            super(v);
            tvTracking = v.findViewById(R.id.tv_tracking);
            tvStatus = v.findViewById(R.id.tv_status);
            tvCarrier = v.findViewById(R.id.tv_carrier);
            tvOrderId = v.findViewById(R.id.tv_order_id);
        }

        void bind(Shipment shipment) {
            tvTracking.setText(shipment.getTrackingNumber() != null
                    ? shipment.getTrackingNumber() : "--");
            tvCarrier.setText("Nha van chuyen: " + safe(shipment.getCarrier()));
            tvOrderId.setText("Don hang: " + safe(shipment.getOrderId()));

            String statusLabel = ShipmentUiMapper.getStatusLabel(shipment.getCurrentStatus());
            tvStatus.setText(statusLabel);
            int colorRes = ShipmentUiMapper.getStatusColor(shipment.getCurrentStatus());
            tvStatus.setTextColor(context.getResources().getColor(colorRes, null));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onShipmentClick(shipment);
            });
        }

        private String safe(String value) {
            return value != null && !value.isEmpty() ? value : "--";
        }
    }
}
