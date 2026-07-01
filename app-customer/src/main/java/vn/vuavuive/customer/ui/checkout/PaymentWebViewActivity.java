package vn.vuavuive.customer.ui.checkout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.OrderViewModel;

@AndroidEntryPoint
public class PaymentWebViewActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private String orderId;
    private String provider;
    private boolean checkingStatus;

    private static final String MOMO_RETURN_URL = "/api/payments/momo/return";
    private static final String ZALOPAY_RETURN_URL = "/api/payments/zalopay/return";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_webview);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderId = getIntent().getStringExtra("order_id");
        provider = getIntent().getStringExtra("provider");

        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progress_bar);
        setupWebView(getIntent().getStringExtra("payment_url"));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(android.view.View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(android.view.View.GONE);
                handleUrl(url);
            }
        });

        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "Khong tai duoc trang thanh toan", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            webView.loadUrl(url);
        }
    }

    private boolean handleUrl(String url) {
        if (url == null) return false;
        if (url.contains(MOMO_RETURN_URL) || url.contains("/api/payment/momo/return")) {
            checkMomoStatus();
            return true;
        }
        if (url.contains(ZALOPAY_RETURN_URL)) {
            checkMomoStatus();
            return true;
        }
        return false;
    }

    private void checkMomoStatus() {
        if (checkingStatus || orderId == null) return;
        checkingStatus = true;
        orderViewModel.getPaymentStatus(orderId).observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                String status = result.data.getPaymentStatus();
                if ("PAID".equalsIgnoreCase(status)) {
                    handlePaymentSuccess();
                } else if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                    handlePaymentFailure("Thanh toan that bai");
                } else {
                    Toast.makeText(this, "Thanh toan dang cho xac nhan", Toast.LENGTH_LONG).show();
                    goToOrders();
                }
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                checkingStatus = false;
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handlePaymentSuccess() {
        cartViewModel.clearCart();
        Toast.makeText(this, "Thanh toan thanh cong!", Toast.LENGTH_LONG).show();
        goToOrders();
    }

    private void handlePaymentFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void goToOrders() {
        Intent intent = new Intent(this, vn.vuavuive.customer.ui.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("navigate_to", "orders");
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if ("MOMO".equalsIgnoreCase(provider)) {
            new AlertDialog.Builder(this)
                    .setMessage("Do you want to cancel this payment?")
                    .setPositiveButton("Exit", (d, w) -> finish())
                    .setNegativeButton("Continue", null)
                    .show();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
