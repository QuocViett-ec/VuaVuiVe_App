package vn.vuavuive.customer.ui.review;

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
import vn.vuavuive.shared.data.dto.Review;
import java.util.ArrayList;
import java.util.List;

public class MyReviewsAdapter extends RecyclerView.Adapter<MyReviewsAdapter.MyReviewVH> {

    private final Context context;
    private List<Review> reviews = new ArrayList<>();

    public MyReviewsAdapter(Context context) {
        this.context = context;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyReviewVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_my_review, parent, false);
        return new MyReviewVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewVH holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class MyReviewVH extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvDate, tvComment;
        RatingBar ratingBar;

        MyReviewVH(View v) {
            super(v);
            ivProduct = v.findViewById(R.id.iv_product);
            tvProductName = v.findViewById(R.id.tv_product_name);
            tvDate = v.findViewById(R.id.tv_date);
            tvComment = v.findViewById(R.id.tv_comment);
            ratingBar = v.findViewById(R.id.rating_bar);
        }

        void bind(Review r) {
            tvProductName.setText(r.getProductName() != null ? r.getProductName() : "Sản phẩm");
            tvComment.setText(r.getComment() != null ? r.getComment() : "");
            ratingBar.setRating(r.getRating());
            if (r.getCreatedAt() != null && r.getCreatedAt().length() >= 10) {
                tvDate.setText(r.getCreatedAt().substring(0, 10));
            } else {
                tvDate.setText("");
            }

            Glide.with(context)
                    .load(r.getProductImage())
                    .placeholder(R.drawable.ic_image)
                    .centerCrop()
                    .into(ivProduct);
        }
    }
}
