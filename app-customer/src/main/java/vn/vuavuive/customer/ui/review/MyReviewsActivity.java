package vn.vuavuive.customer.ui.review;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.util.SessionManager;
import java.util.ArrayList;

@AndroidEntryPoint
public class MyReviewsActivity extends AppCompatActivity {

    private static final String TAG = "MyReviewsActivity";

    private ProductViewModel productViewModel;
    private MyReviewsAdapter reviewsAdapter;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tvReviewCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        initViews();
        setupRecycler();
        loadReviews();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvReviewCount = findViewById(R.id.tv_review_count);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rv_orders);
        reviewsAdapter = new MyReviewsAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(reviewsAdapter);
    }

    private void loadReviews() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        // Try Firebase Auth UID first (most reliable), then Session userId
        String firebaseUid = null;
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            firebaseUid = fbUser.getUid();
        }

        SessionManager sessionManager = new SessionManager(this);
        String sessionUserId = sessionManager.getUserId();

        Log.d(TAG, "Firebase UID: " + firebaseUid + " | Session userId: " + sessionUserId);

        // Use Firebase UID as the primary (since reviews store firebase auth uid as user_id)
        String userId = firebaseUid != null ? firebaseUid : sessionUserId;

        if (userId == null || userId.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        final String finalUserId = userId;
        final String fallbackUserId = (firebaseUid != null && !firebaseUid.equals(sessionUserId)) ? sessionUserId : null;

        productViewModel.getUserReviews(finalUserId).observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.LOADING) return;

            progressBar.setVisibility(View.GONE);

            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null && !result.data.isEmpty()) {
                // Found reviews with primary ID
                reviewsAdapter.setReviews(result.data);
                layoutEmpty.setVisibility(View.GONE);
                if (tvReviewCount != null) {
                    tvReviewCount.setVisibility(View.VISIBLE);
                    tvReviewCount.setText(result.data.size() + " đánh giá");
                }
            } else if (fallbackUserId != null && !fallbackUserId.isEmpty()) {
                // Try with fallback (session userId)
                Log.d(TAG, "Primary userId found no results, trying fallback: " + fallbackUserId);
                progressBar.setVisibility(View.VISIBLE);
                productViewModel.getUserReviews(fallbackUserId).observe(this, result2 -> {
                    progressBar.setVisibility(View.GONE);
                    if (result2.status == AuthRepository.Result.Status.SUCCESS && result2.data != null && !result2.data.isEmpty()) {
                        reviewsAdapter.setReviews(result2.data);
                        layoutEmpty.setVisibility(View.GONE);
                        if (tvReviewCount != null) {
                            tvReviewCount.setVisibility(View.VISIBLE);
                            tvReviewCount.setText(result2.data.size() + " đánh giá");
                        }
                    } else {
                        reviewsAdapter.setReviews(new ArrayList<>());
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                reviewsAdapter.setReviews(new ArrayList<>());
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
