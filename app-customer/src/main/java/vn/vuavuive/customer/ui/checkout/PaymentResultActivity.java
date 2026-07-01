package vn.vuavuive.customer.ui.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.BuildConfig;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.OrderViewModel;

@AndroidEntryPoint
public class PaymentResultActivity extends AppCompatActivity {
    private OrderViewModel orderViewModel;
    private CartViewModel cartViewModel;
    private TextView tvStatus;
    private TextView tvHint;
    private String orderId;
    private String payUrl;
    private String deeplink;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        tvStatus = findViewById(R.id.tv_payment_status);
        tvHint = findViewById(R.id.tv_payment_hint);
        Button btnCheck = findViewById(R.id.btn_check_payment);
        Button btnOrders = findViewById(R.id.btn_view_orders);
        Button btnOpenPayment = findViewById(R.id.btn_open_payment);
        Button btnMockSuccess = findViewById(R.id.btn_mock_success);
        orderId = getIntent().getStringExtra("order_id");
        payUrl = getIntent().getStringExtra("payment_url");
        deeplink = getIntent().getStringExtra("deeplink");
        provider = getIntent().getStringExtra("provider");
        btnCheck.setOnClickListener(v -> checkPaymentStatus());
        btnOrders.setOnClickListener(v -> goToOrders());
        btnOpenPayment.setOnClickListener(v -> openPayment());
        btnMockSuccess.setVisibility(BuildConfig.DEBUG && supportsMockSuccess() ? View.VISIBLE : View.GONE);
        btnMockSuccess.setOnClickListener(v -> mockPaymentSuccess());
        showStatus("Chua xac nhan thanh toan", "Hoan tat thanh toan tren trinh duyet roi bam 'Kiem tra thanh toan'.");
        openPayment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPaymentStatus();
    }

    private void openPayment() {
        String url = payUrl != null && !payUrl.isEmpty() ? payUrl : deeplink;
        if (url == null || url.isEmpty()) {
            showStatus("Khong co lien ket thanh toan", "Vui long kiem tra lai giao dich.");
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            if (deeplink != null && !deeplink.isEmpty() && !deeplink.equals(url)) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(deeplink)));
            } else {
                Toast.makeText(this, "Khong mo duoc cong thanh toan", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkPaymentStatus() {
        if (orderId == null || orderId.isEmpty()) {
            return;
        }
        showStatus("Dang kiem tra thanh toan", "Backend se xac nhan ket qua tu " + providerLabel() + ".");
        orderViewModel.getPaymentStatus(orderId).observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                String status = result.data.getPaymentStatus();
                String orderStatus = result.data.getOrderStatus();
                if ("PAID".equalsIgnoreCase(status)) {
                    cartViewModel.clearCart();
                    showStatus("Thanh toan thanh cong",
                            "Don hang da thanh toan va dang cho admin duyet (" + orderStatus + ").");
                } else if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                    showStatus("Thanh toan that bai", "Ban co the thu thanh toan lai.");
                } else {
                    showStatus("Thanh toan dang cho xac nhan",
                            "Trang thai hien tai: " + status + " / " + orderStatus + ".");
                }
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                showStatus("Khong kiem tra duoc thanh toan", result.message);
            }
        });
    }

    private void mockPaymentSuccess() {
        androidx.lifecycle.LiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.PaymentStatusResponse>> call =
                "ZALOPAY".equalsIgnoreCase(provider)
                        ? orderViewModel.mockZaloPaySuccess(orderId)
                        : orderViewModel.mockMomoSuccess(orderId);
        call.observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS) {
                checkPaymentStatus();
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean supportsMockSuccess() {
        return "MOMO".equalsIgnoreCase(provider) || "ZALOPAY".equalsIgnoreCase(provider);
    }

    private String providerLabel() {
        if ("ZALOPAY".equalsIgnoreCase(provider)) return "ZaloPay";
        return "MoMo";
    }

    private void showStatus(String text, String hint) {
        tvStatus.setText(text);
        if (tvHint != null) {
            tvHint.setText(hint);
        }
    }

    private void goToOrders() {
        Intent intent = new Intent(this, vn.vuavuive.customer.ui.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("navigate_to", "orders");
        startActivity(intent);
        finish();
    }
}
