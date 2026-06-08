package vn.vuavuive.customer.ui.product;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import vn.vuavuive.customer.R;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    private final Context context;
    private final OnProductClickListener listener;
    private OnAddToCartListener addToCartListener;
    private List<Product> products = new ArrayList<>();

    public ProductAdapter(Context context, OnProductClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setAddToCartListener(OnAddToCartListener addToCartListener) {
        this.addToCartListener = addToCartListener;
    }

    public void setProducts(List<Product> newProducts) {
        final List<Product> oldList = this.products;
        final List<Product> newList = newProducts != null ? newProducts : new ArrayList<>();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldList.get(o).getId() != null &&
                        oldList.get(o).getId().equals(newList.get(n).getId());
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return oldList.get(o).getId() != null &&
                        oldList.get(o).getId().equals(newList.get(n).getId());
            }
        });

        this.products = newList;
        diffResult.dispatchUpdatesTo(this);
    }

    public void appendProducts(List<Product> newProducts) {
        if (newProducts == null || newProducts.isEmpty()) return;
        int start = products.size();
        products = new ArrayList<>(products);
        products.addAll(newProducts);
        notifyItemRangeInserted(start, newProducts.size());
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() { return products.size(); }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvOriginalPrice, tvDiscount, tvUnit, tvRatingCount, tvSoldCount, tvHotBadge;
        RatingBar ratingBar;
        View layoutOutOfStock;
        MaterialButton btnQuickAdd;

        ProductViewHolder(View itemView) {
            super(itemView);
            ivProduct        = itemView.findViewById(R.id.iv_product);
            tvName           = itemView.findViewById(R.id.tv_product_name);
            tvPrice          = itemView.findViewById(R.id.tv_price);
            tvOriginalPrice  = itemView.findViewById(R.id.tv_original_price);
            tvDiscount       = itemView.findViewById(R.id.tv_discount);
            tvUnit           = itemView.findViewById(R.id.tv_unit);
            ratingBar        = itemView.findViewById(R.id.rating_bar);
            tvRatingCount    = itemView.findViewById(R.id.tv_rating_count);
            tvSoldCount      = itemView.findViewById(R.id.tv_sold_count);
            tvHotBadge       = itemView.findViewById(R.id.tv_hot_badge);
            layoutOutOfStock = itemView.findViewById(R.id.layout_out_of_stock);
            btnQuickAdd      = itemView.findViewById(R.id.btn_quick_add);
        }

        void bind(Product product) {
            // Name
            tvName.setText(product.getName());

            // Price
            tvPrice.setText(CurrencyFormatter.format(product.getPrice()));
            tvUnit.setText(product.getUnit() != null ? "/" + product.getUnit() : "");

            // Original price & discount badge
            if (product.getOriginalPrice() != null && product.getOriginalPrice() > product.getPrice()) {
                tvOriginalPrice.setText(CurrencyFormatter.format(product.getOriginalPrice()));
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvOriginalPrice.setVisibility(View.VISIBLE);
                int discountPct = (int) ((1 - product.getPrice() / product.getOriginalPrice()) * 100);
                tvDiscount.setText("-" + discountPct + "%");
                tvDiscount.setVisibility(View.VISIBLE);
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscount.setVisibility(View.GONE);
            }

            // Rating
            if (product.getRating() != null) {
                ratingBar.setRating(product.getRating().floatValue());
                ratingBar.setVisibility(View.VISIBLE);
                String countText = product.getReviewCount() != null
                        ? "(" + product.getReviewCount() + ")" : "";
                tvRatingCount.setText(countText);
            } else {
                ratingBar.setVisibility(View.INVISIBLE);
                tvRatingCount.setText("");
            }

            // Sold count
            if (tvSoldCount != null) {
                if (product.getSoldCount() != null && product.getSoldCount() > 0) {
                    String sold = product.getSoldCount() >= 1000
                            ? (product.getSoldCount() / 1000) + "K"
                            : String.valueOf(product.getSoldCount());
                    tvSoldCount.setText("🛒 " + sold);
                    tvSoldCount.setVisibility(View.VISIBLE);
                } else {
                    tvSoldCount.setVisibility(View.GONE);
                }
            }

            // HOT badge for high-selling items
            if (tvHotBadge != null) {
                boolean isHot = product.getSoldCount() != null && product.getSoldCount() > 1500;
                tvHotBadge.setVisibility(isHot ? View.VISIBLE : View.GONE);
            }

            // Out of stock overlay
            if (layoutOutOfStock != null) {
                layoutOutOfStock.setVisibility(product.getStock() <= 0 ? View.VISIBLE : View.GONE);
            }

            // Quick add to cart button
            if (btnQuickAdd != null) {
                if (product.getStock() <= 0) {
                    btnQuickAdd.setAlpha(0.4f);
                    btnQuickAdd.setEnabled(false);
                } else {
                    btnQuickAdd.setAlpha(1.0f);
                    btnQuickAdd.setEnabled(true);
                    btnQuickAdd.setOnClickListener(v -> {
                        if (addToCartListener != null) {
                            addToCartListener.onAddToCart(product);
                        } else {
                            // Default feedback
                            Toast.makeText(context, "Đã thêm " + product.getName() + " vào giỏ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            // Image
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .centerCrop()
                    .into(ivProduct);

            // Item click
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });
        }
    }
}
