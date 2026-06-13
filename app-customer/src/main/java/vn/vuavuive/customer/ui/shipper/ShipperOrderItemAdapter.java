package vn.vuavuive.customer.ui.shipper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import vn.vuavuive.customer.R;
import vn.vuavuive.shared.data.dto.OrderItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * ShipperOrderItemAdapter — Hiển thị danh sách sản phẩm trong đơn hàng (màn hình chi tiết Shipper).
 */
public class ShipperOrderItemAdapter extends RecyclerView.Adapter<ShipperOrderItemAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderItem> items;

    public ShipperOrderItemAdapter(Context context, List<OrderItem> items) {
        this.context = context;
        this.items   = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvSubtotal;

        ViewHolder(View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tv_product_name);
            tvQty      = itemView.findViewById(R.id.tv_quantity);
            tvSubtotal = itemView.findViewById(R.id.tv_subtotal);
        }

        void bind(OrderItem item) {
            tvName.setText(item.getProductName() != null ? item.getProductName() : "Sản phẩm");
            tvQty.setText("x" + item.getQuantity());
            NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            tvSubtotal.setText(fmt.format((long) item.getLineTotal()) + " đ");
        }
    }
}
