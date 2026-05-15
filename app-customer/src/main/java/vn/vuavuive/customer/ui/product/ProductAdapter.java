package vn.vuavuive.customer.ui.product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import vn.vuavuive.customer.R;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final Context context;
    private final OnProductClickListener listener;
    private List<Product> products = new ArrayList<>();

    public ProductAdapter(Context context, OnProductClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void appendProducts(List<Product> newProducts) {
        int start = products.size();
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
        TextView tvName, tvPrice, tvOriginalPrice, tvDiscount, tvUnit, tvRatingCount;
        RatingBar ratingBar;

        ProductViewHolder(View itemView) {
            super(itemView);
            ivProduct       = itemView.findViewById(R.id.iv_product);
            tvName          = itemView.findViewById(R.id.tv_product_name);
            tvPrice         = itemView.findViewById(R.id.tv_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscount      = itemView.findViewById(R.id.tv_discount);
            tvUnit          = itemView.findViewById(R.id.tv_unit);
            ratingBar       = itemView.findViewById(R.id.rating_bar);
            tvRatingCount   = itemView.findViewById(R.id.tv_rating_count);
        }

        void bind(Product product) {
            tvName.setText(product.getName());
            tvPrice.setText(CurrencyFormatter.format(product.getPrice()));
            tvUnit.setText(product.getUnit() != null ? "/" + product.getUnit() : "");

            // Original price & discount badge
            if (product.getOriginalPrice() != null && product.getOriginalPrice() > product.getPrice()) {
                tvOriginalPrice.setText(CurrencyFormatter.format(product.getOriginalPrice()));
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
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

            // Image
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(ivProduct);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });
        }
    }
}
