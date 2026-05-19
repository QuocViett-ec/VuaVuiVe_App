package vn.vuavuive.customer.ui.shipment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import vn.vuavuive.customer.R;
import vn.vuavuive.shared.data.dto.StatusEvent;
import java.util.ArrayList;
import java.util.List;

public class ShipmentStatusAdapter extends RecyclerView.Adapter<ShipmentStatusAdapter.StatusVH> {

    private final Context context;
    private List<StatusEvent> items = new ArrayList<>();

    public ShipmentStatusAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<StatusEvent> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatusVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_shipment_status, parent, false);
        return new StatusVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusVH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class StatusVH extends RecyclerView.ViewHolder {
        TextView tvStatus, tvTime, tvNote;

        StatusVH(View v) {
            super(v);
            tvStatus = v.findViewById(R.id.tv_status);
            tvTime = v.findViewById(R.id.tv_time);
            tvNote = v.findViewById(R.id.tv_note);
        }

        void bind(StatusEvent event) {
            tvStatus.setText(ShipmentUiMapper.getStatusLabel(event.getStatus()));
            tvTime.setText(event.getTimestamp() != null ? event.getTimestamp() : "--");
            if (event.getNote() != null && !event.getNote().isEmpty()) {
                tvNote.setText(event.getNote());
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }
        }
    }
}
