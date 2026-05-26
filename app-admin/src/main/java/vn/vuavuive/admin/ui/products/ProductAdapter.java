package vn.vuavuive.admin.ui.products;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.databinding.ItemProductBinding;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.util.CurrencyFormatter;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onProductLongClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public void updateData(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        public ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            binding.tvProductName.setText(product.getName());
            binding.tvCategory.setText(product.getSubCategory() != null ? product.getSubCategory() : product.getCategory());
            binding.tvPrice.setText(CurrencyFormatter.formatVnd(product.getPrice()));
            binding.tvUnit.setText(product.getUnit() != null ? " / " + product.getUnit() : "");

            // Original price handling
            if (product.isOnSale() && product.getOriginalPrice() != null) {
                binding.tvOriginalPrice.setVisibility(View.VISIBLE);
                binding.tvOriginalPrice.setText(CurrencyFormatter.formatVnd(product.getOriginalPrice()));
                binding.tvOriginalPrice.setPaintFlags(binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                binding.tvOriginalPrice.setVisibility(View.GONE);
            }

            // Stock level alarms
            binding.tvStock.setText("Tồn kho: " + product.getStock());
            if (product.getStock() == 0) {
                binding.tvStock.setTextColor(Color.parseColor("#F44336")); // Crimson Red
                binding.tvStock.setText("Hết hàng!");
            } else if (product.getStock() <= 10) {
                binding.tvStock.setTextColor(Color.parseColor("#FF9800")); // Warning Orange
                binding.tvStock.setText("Sắp hết: " + product.getStock());
            } else {
                binding.tvStock.setTextColor(Color.parseColor("#4CAF50")); // Safe Green
            }

            int sold = product.getSoldCount() != null ? product.getSoldCount() : 0;
            binding.tvSold.setText("Đã bán: " + sold);

            // Active / Inactive status badge
            if (product.isActive()) {
                binding.tvStatus.setText("ACTIVE");
                binding.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#334CAF50")));
            } else {
                binding.tvStatus.setText("INACTIVE");
                binding.tvStatus.setTextColor(Color.parseColor("#F44336"));
                binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#33F44336")));
            }

            // Image loading with Glide
            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(binding.ivProductImage);

            // Click hooks
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                } else {
                    // Default behavior: launch edit
                    Intent intent = new Intent(itemView.getContext(), ProductEditActivity.class);
                    intent.putExtra("PRODUCT_ID", product.getId());
                    itemView.getContext().startActivity(intent);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onProductLongClick(product);
                    return true;
                }
                return false;
            });
        }
    }
}
