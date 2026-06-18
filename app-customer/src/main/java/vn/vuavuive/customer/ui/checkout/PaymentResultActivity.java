package vn.vuavuive.customer.ui.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
import vn.vuavuive.shared.data.dto.PaymentDetail;

@AndroidEntryPoint
public class PaymentResultActivity extends AppCompatActivity {
    private OrderViewModel orderViewModel;
    private CartViewModel cartViewModel;
    private TextView tvStatus;
    private String orderId;
    private boolean openedPayment;
    private boolean checkedAfterReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        tvStatus = findViewById(R.id.tv_payment_status);
        Button btnOrders = findViewById(R.id.btn_view_orders);
        orderId = getIntent().getStringExtra("order_id");
        btnOrders.setOnClickListener(v -> goToOrders());
        openPayment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (openedPayment && !checkedAfterReturn) {
            checkedAfterReturn = true;
            refreshOrder();
        }
    }

    private void openPayment() {
        String deeplink = getIntent().getStringExtra("deeplink");
        String payUrl = getIntent().getStringExtra("payment_url");
        String url = deeplink != null && !deeplink.isEmpty() ? deeplink : payUrl;
        if (url == null || url.isEmpty()) {
            showStatus("Waiting for payment confirmation");
            return;
        }
        openedPayment = true;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            if (payUrl != null && !payUrl.equals(url)) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl)));
            } else {
                Toast.makeText(this, "Khong mo duoc MoMo", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void refreshOrder() {
        showStatus("Waiting for payment confirmation");
        orderViewModel.getOrderDetail(orderId).observe(this, result -> {
            if (result.status != AuthRepository.Result.Status.SUCCESS || result.data == null) return;
            PaymentDetail payment = result.data.getPayment();
            String status = payment == null ? result.data.getStatus() : payment.getStatus();
            if ("paid".equalsIgnoreCase(status)) {
                cartViewModel.clearCart();
                showStatus("Payment successful");
            } else if ("failed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
                showStatus("Payment failed");
            } else {
                showStatus("Waiting for payment confirmation");
            }
        });
    }

    private void showStatus(String text) {
        tvStatus.setText(text);
    }

    private void goToOrders() {
        Intent intent = new Intent(this, vn.vuavuive.customer.ui.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("navigate_to", "orders");
        startActivity(intent);
        finish();
    }
}
