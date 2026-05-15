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
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import androidx.lifecycle.ViewModelProvider;

@AndroidEntryPoint
public class PaymentWebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private CartViewModel cartViewModel;
    private String orderId;

    // VNPay success callback pattern
    private static final String VNPAY_RETURN_URL = "/api/payment/vnpay/return";
    // MoMo success callback pattern
    private static final String MOMO_RETURN_URL = "/api/payment/momo/return";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_webview);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderId = getIntent().getStringExtra("order_id");
        String paymentUrl = getIntent().getStringExtra("payment_url");

        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progress_bar);

        setupWebView(paymentUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String requestUrl = request.getUrl().toString();
                return handleUrl(requestUrl);
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

        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            Toast.makeText(this, "Không tải được trang thanh toán", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean handleUrl(String url) {
        if (url == null) return false;

        // Check VNPay return
        if (url.contains(VNPAY_RETURN_URL)) {
            Uri uri = Uri.parse(url);
            String responseCode = uri.getQueryParameter("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                handlePaymentSuccess();
            } else {
                handlePaymentFailure("Thanh toán VNPay thất bại (code: " + responseCode + ")");
            }
            return true;
        }

        // Check MoMo return
        if (url.contains(MOMO_RETURN_URL)) {
            Uri uri = Uri.parse(url);
            String resultCode = uri.getQueryParameter("resultCode");
            if ("0".equals(resultCode)) {
                handlePaymentSuccess();
            } else {
                handlePaymentFailure("Thanh toán MoMo thất bại (code: " + resultCode + ")");
            }
            return true;
        }

        return false;
    }

    private void handlePaymentSuccess() {
        cartViewModel.clearCart();
        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
        // Go back to main with order list tab
        Intent intent = new Intent(this, vn.vuavuive.customer.ui.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("navigate_to", "orders");
        startActivity(intent);
        finish();
    }

    private void handlePaymentFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
