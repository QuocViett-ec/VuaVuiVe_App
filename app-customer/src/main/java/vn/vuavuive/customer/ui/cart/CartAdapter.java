package vn.vuavuive.customer.ui.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.checkbox.MaterialCheckBox;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final CartViewModel cartViewModel;
    private final boolean savedMode;
    private List<CartItemEntity> items = new ArrayList<>();
    private final Set<String> selectedProductIds = new HashSet<>();
    private OnSelectionChangedListener selectionChangedListener;

    public CartAdapter(Context context, CartViewModel cartViewModel, boolean savedMode) {
        this.context = context;
        this.cartViewModel = cartViewModel;
        this.savedMode = savedMode;
    }

    public void setItems(List<CartItemEntity> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        pruneSelections();
        notifyDataSetChanged();
    }

    public CartItemEntity getItemAt(int position) {
        if (position >= 0 && position < items.size()) return items.get(position);
        return null;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public List<String> getSelectedProductIds() {
        return new ArrayList<>(selectedProductIds);
    }

    public void clearSelection() {
        if (selectedProductIds.isEmpty()) return;
        selectedProductIds.clear();
        notifySelectionChanged();
        notifyDataSetChanged();
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(getItemAt(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    private void pruneSelections() {
        if (selectedProductIds.isEmpty()) return;
        Set<String> currentIds = new HashSet<>();
        for (CartItemEntity item : items) {
            if (item != null && item.getProductId() != null) currentIds.add(item.getProductId());
        }
        if (selectedProductIds.retainAll(currentIds)) {
            notifySelectionChanged();
        }
    }

    private void toggleSelection(CartItemEntity item) {
        if (savedMode || !hasProductId(item)) return;
        String productId = item.getProductId();
        if (selectedProductIds.contains(productId)) {
            selectedProductIds.remove(productId);
        } else {
            selectedProductIds.add(productId);
        }
        notifySelectionChanged();
        int position = items.indexOf(item);
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedProductIds.size());
        }
    }

    private boolean hasProductId(CartItemEntity item) {
        return item != null && item.getProductId() != null && !item.getProductId().isEmpty();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvSubtotal, tvQuantity;
        ImageView btnDecrease, btnIncrease;
        ImageView btnRemove;
        TextView tvActionSave, tvActionMove;
        MaterialCheckBox cbSelectItem;

        CartViewHolder(View itemView) {
            super(itemView);
            cbSelectItem = itemView.findViewById(R.id.cb_select_item);
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
            if (item == null) return;

            tvName.setText(item.getProductName() != null ? item.getProductName() : "Sản phẩm");
            tvPrice.setText(CurrencyFormatter.format(item.getProductPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvSubtotal.setText(CurrencyFormatter.format(item.getLineTotal()));

            Glide.with(context).load(item.getProductImageUrl())
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(ivProduct);

            bindSelection(item);

            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() <= 1) {
                    if (hasProductId(item)) {
                        cartViewModel.removeItem(item.getProductId());
                        selectedProductIds.remove(item.getProductId());
                        notifySelectionChanged();
                        Toast.makeText(context, "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                if (hasProductId(item)) {
                    cartViewModel.updateQuantity(item.getProductId(), item.getQuantity() - 1);
                }
            });

            btnIncrease.setOnClickListener(v -> {
                if (item.getQuantity() < item.getProductStock()) {
                    if (hasProductId(item)) {
                        cartViewModel.updateQuantity(item.getProductId(), item.getQuantity() + 1);
                    }
                } else {
                    Toast.makeText(context, "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
                }
            });

            btnRemove.setOnClickListener(v -> {
                if (hasProductId(item)) {
                    cartViewModel.removeItem(item.getProductId());
                    selectedProductIds.remove(item.getProductId());
                    notifySelectionChanged();
                }
            });

            if (savedMode) {
                tvActionSave.setVisibility(View.GONE);
                tvActionMove.setVisibility(View.VISIBLE);
                tvActionMove.setOnClickListener(v -> {
                    if (hasProductId(item)) cartViewModel.moveToCart(item.getProductId());
                });
            } else {
                tvActionSave.setVisibility(View.VISIBLE);
                tvActionMove.setVisibility(View.GONE);
                tvActionSave.setOnClickListener(v -> {
                    if (hasProductId(item)) cartViewModel.saveForLater(item.getProductId());
                });
            }
        }

        private void bindSelection(CartItemEntity item) {
            cbSelectItem.setOnCheckedChangeListener(null);
            if (savedMode) {
                cbSelectItem.setVisibility(View.GONE);
                itemView.setOnClickListener(null);
                return;
            }

            cbSelectItem.setVisibility(View.VISIBLE);
            cbSelectItem.setChecked(hasProductId(item) && selectedProductIds.contains(item.getProductId()));
            cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!hasProductId(item)) return;
                if (isChecked) {
                    selectedProductIds.add(item.getProductId());
                } else {
                    selectedProductIds.remove(item.getProductId());
                }
                notifySelectionChanged();
            });
            itemView.setOnClickListener(v -> toggleSelection(item));
        }
    }
}
