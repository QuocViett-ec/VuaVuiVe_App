package vn.vuavuive.customer.ui.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final CartViewModel cartViewModel;
    private final boolean savedMode;
    private List<CartItemEntity> items = new ArrayList<>();

    public CartAdapter(Context context, CartViewModel cartViewModel, boolean savedMode) {
        this.context = context;
        this.cartViewModel = cartViewModel;
        this.savedMode = savedMode;
    }

    public void setItems(List<CartItemEntity> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public CartItemEntity getItemAt(int position) {
        if (position >= 0 && position < items.size()) return items.get(position);
        return null;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvSubtotal, tvQuantity;
        TextView btnDecrease, btnIncrease;
        ImageButton btnRemove;
        TextView tvActionSave, tvActionMove;

        CartViewHolder(View itemView) {
            super(itemView);
            ivProduct   = itemView.findViewById(R.id.iv_product);
            tvName      = itemView.findViewById(R.id.tv_product_name);
            tvPrice     = itemView.findViewById(R.id.tv_price);
            tvSubtotal  = itemView.findViewById(R.id.tv_subtotal);
            tvQuantity  = itemView.findViewById(R.id.tv_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnRemove   = itemView.findViewById(R.id.btn_remove);
            tvActionSave = itemView.findViewById(R.id.tv_action_save);
            tvActionMove = itemView.findViewById(R.id.tv_action_move);
        }

        void bind(CartItemEntity item) {
            tvName.setText(item.getProductName());
            tvPrice.setText(CurrencyFormatter.format(item.getProductPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvSubtotal.setText(CurrencyFormatter.format(item.getLineTotal()));

            Glide.with(context).load(item.getProductImageUrl())
                    .placeholder(R.drawable.ic_image).into(ivProduct);

            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() <= 1) {
                    Toast.makeText(context, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
                    return;
                }
                cartViewModel.updateQuantity(item.getProductId(), item.getQuantity() - 1);
            });

            btnIncrease.setOnClickListener(v -> {
                if (item.getQuantity() < item.getProductStock()) {
                    cartViewModel.updateQuantity(item.getProductId(), item.getQuantity() + 1);
                }
            });

            btnRemove.setOnClickListener(v -> {
                cartViewModel.removeItem(item.getProductId());
            });

            if (savedMode) {
                tvActionSave.setVisibility(View.GONE);
                tvActionMove.setVisibility(View.VISIBLE);
                tvActionMove.setOnClickListener(v -> cartViewModel.moveToCart(item.getProductId()));
            } else {
                tvActionSave.setVisibility(View.VISIBLE);
                tvActionMove.setVisibility(View.GONE);
                tvActionSave.setOnClickListener(v -> cartViewModel.saveForLater(item.getProductId()));
            }
        }
    }
}
