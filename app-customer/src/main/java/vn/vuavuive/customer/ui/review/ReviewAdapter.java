package vn.vuavuive.customer.ui.review;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import vn.vuavuive.customer.R;
import vn.vuavuive.shared.data.dto.Review;
import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewVH> {

    private final Context context;
    private List<Review> reviews = new ArrayList<>();

    public ReviewAdapter(Context context) {
        this.context = context;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ReviewVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewVH holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override public int getItemCount() { return reviews.size(); }

    static class ReviewVH extends RecyclerView.ViewHolder {
        TextView tvUserName, tvComment, tvDate;
        RatingBar ratingBar;

        ReviewVH(View v) {
            super(v);
            tvUserName = v.findViewById(R.id.tv_user_name);
            tvComment  = v.findViewById(R.id.tv_comment);
            tvDate     = v.findViewById(R.id.tv_date);
            ratingBar  = v.findViewById(R.id.rating_bar);
        }

        void bind(Review review) {
            // Use userName if available (from mock data), else derive from userId
            String userName = review.getUserName();
            if (userName != null && !userName.isEmpty()) {
                tvUserName.setText(userName);
            } else {
                String uid = review.getUserId();
                String userDisplay = (uid != null && uid.length() >= 8)
                        ? "Người dùng " + uid.substring(0, 4).toUpperCase()
                        : "Khách hàng";
                tvUserName.setText(userDisplay);
            }
            tvComment.setText(review.getComment() != null ? review.getComment() : "");
            ratingBar.setRating(review.getRating());
            if (review.getCreatedAt() != null && review.getCreatedAt().length() >= 10) {
                tvDate.setText(review.getCreatedAt().substring(0, 10));
            }
        }
    }
}
