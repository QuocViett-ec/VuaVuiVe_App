package vn.vuavuive.customer.ui.review;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.order.OrderAdapter;
import vn.vuavuive.customer.ui.order.OrderDetailActivity;
import vn.vuavuive.customer.viewmodel.OrderViewModel;

@AndroidEntryPoint
public class MyReviewsActivity extends AppCompatActivity {

    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        initViews();
        setupRecycler();
        loadOrders();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rv_orders);
        orderAdapter = new OrderAdapter(this, order -> {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        orderViewModel.getOrders("delivered", 1).observe(this, result -> {
            progressBar.setVisibility(View.GONE);
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                orderAdapter.setOrders(result.data);
                tvEmpty.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
