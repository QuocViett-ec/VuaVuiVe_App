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
import vn.vuavuive.shared.data.dto.OrderItem;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ItemVH> {

    private final Context context;
    private List<OrderItem> items = new ArrayList<>();

    public OrderItemAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new ItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemVH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override public int getItemCount() { return items.size(); }

    static class ItemVH extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvQtyPrice, tvTotal;

        ItemVH(View v) {
            super(v);
            ivProduct  = v.findViewById(R.id.iv_product);
            tvName     = v.findViewById(R.id.tv_product_name);
            tvQtyPrice = v.findViewById(R.id.tv_qty_price);
            tvTotal    = v.findViewById(R.id.tv_line_total);
        }

        void bind(OrderItem item) {
            Glide.with(ivProduct.getContext())
                    .load(item.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(ivProduct);
            tvName.setText(item.getName());
            tvQtyPrice.setText(item.getQuantity() + " x " + CurrencyFormatter.format(item.getPrice()));
            tvTotal.setText(CurrencyFormatter.format(item.getLineTotal()));
        }
    }
}
