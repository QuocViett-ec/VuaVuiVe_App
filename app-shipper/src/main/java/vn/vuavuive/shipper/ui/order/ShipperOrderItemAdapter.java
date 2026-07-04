package vn.vuavuive.shipper.ui.order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import vn.vuavuive.shipper.R;
import vn.vuavuive.shared.data.dto.OrderItem;

public class ShipperOrderItemAdapter extends RecyclerView.Adapter<ShipperOrderItemAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderItem> items;

    public ShipperOrderItemAdapter(Context context, List<OrderItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvQty, tvSubtotal;

        ViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvQty = itemView.findViewById(R.id.tv_qty_price);
            tvSubtotal = itemView.findViewById(R.id.tv_line_total);
        }

        void bind(OrderItem item) {
            tvName.setText(item.getProductName() != null ? item.getProductName() : "Sản phẩm");
            tvQty.setText("x" + item.getQuantity());

            NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            tvSubtotal.setText(fmt.format((long) item.getLineTotal()) + " đ");

            loadImage(item);
        }

        private void loadImage(OrderItem item) {
            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(itemView)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(ivProduct);
                return;
            }

            Glide.with(itemView)
                    .load(android.R.drawable.ic_menu_gallery)
                    .into(ivProduct);

            String productId = item.getProductId();
            if (productId == null || productId.trim().isEmpty()) return;

            FirebaseDatabase.getInstance().getReference("products").child(productId).get()
                    .addOnSuccessListener(snapshot -> {
                        String fallbackUrl = snapshot.child("image_url").getValue(String.class);
                        if (fallbackUrl == null || fallbackUrl.trim().isEmpty()) {
                            fallbackUrl = snapshot.child("imageUrl").getValue(String.class);
                        }
                        if (fallbackUrl != null && !fallbackUrl.trim().isEmpty()) {
                            Glide.with(itemView)
                                    .load(fallbackUrl)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_gallery)
                                    .into(ivProduct);
                        }
                    });
        }
    }
}
