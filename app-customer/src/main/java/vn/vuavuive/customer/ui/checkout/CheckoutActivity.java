package vn.vuavuive.customer.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.auth.LoginActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.data.dto.DeliveryInfo;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.Constants;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AndroidEntryPoint
public class CheckoutActivity extends AppCompatActivity {

    private OrderViewModel orderViewModel;
    private CartViewModel cartViewModel;
    private ProductViewModel productViewModel;
    private AuthViewModel authViewModel;

    private TextInputEditText etName, etPhone, etAddress, etVoucher, etNote;
    private RadioGroup rgPaymentMethod;
    private MaterialButton btnPlaceOrder, btnApplyVoucher;
    private ProgressBar progressBar;
    private android.widget.TextView tvSubtotal, tvShippingFee, tvDiscount, tvTotal;
    private android.widget.LinearLayout layoutDiscount;
    private double appliedDiscount = 0;

    private List<CartItemEntity> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        cartViewModel  = new ViewModelProvider(this).get(CartViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        if (!authViewModel.isLoggedIn()) {
            Toast.makeText(this, R.string.login_required_checkout, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        observeCart();
        setupPlaceOrder();
    }

    private void initViews() {
        etName      = findViewById(R.id.et_receiver_name);
        etPhone     = findViewById(R.id.et_receiver_phone);
        etAddress   = findViewById(R.id.et_delivery_address);
        etVoucher   = findViewById(R.id.et_voucher_code);
        btnApplyVoucher = findViewById(R.id.btn_apply_voucher);
        etNote      = findViewById(R.id.et_note);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        progressBar  = findViewById(R.id.progress_bar);
        tvSubtotal    = findViewById(R.id.tv_subtotal);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvDiscount    = findViewById(R.id.tv_discount);
        tvTotal       = findViewById(R.id.tv_total);
        layoutDiscount = findViewById(R.id.layout_discount);

        if (btnApplyVoucher != null) {
            btnApplyVoucher.setOnClickListener(v -> applyVoucherCode());
        }
    }

    private void observeCart() {
        cartViewModel.getCartItems().observe(this, items -> {
            cartItems = items != null ? items : new ArrayList<>();
            updatePriceSummary();
        });
    }

    private double getSubtotal() {
        double subtotal = 0;
        for (CartItemEntity ci : cartItems) {
            subtotal += ci.getLineTotal();
        }
        return subtotal;
    }

    private void applyVoucherCode() {
        String code = getText(etVoucher);
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("VUAVUIVE".equalsIgnoreCase(code)) {
            double sub = getSubtotal();
            appliedDiscount = sub * 0.15;
            Toast.makeText(this, "Áp dụng mã thành công! Giảm 15%", Toast.LENGTH_SHORT).show();
        } else if ("FREESHIP24".equalsIgnoreCase(code) || "FREESHIP".equalsIgnoreCase(code)) {
            appliedDiscount = 30000;
            Toast.makeText(this, "Áp dụng mã thành công! Miễn phí ship", Toast.LENGTH_SHORT).show();
        } else {
            appliedDiscount = 0;
            Toast.makeText(this, "Mã không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
        }
        updatePriceSummary();
    }

    private void updatePriceSummary() {
        double subtotal = getSubtotal();
        double shippingFee = 30000; // default 30k
        double discount = appliedDiscount;
        double total = subtotal + shippingFee - discount;
        if (total < 0) total = 0;

        if (tvSubtotal != null) {
            tvSubtotal.setText(CurrencyFormatter.format(subtotal));
        }
        if (tvShippingFee != null) {
            tvShippingFee.setText(CurrencyFormatter.format(shippingFee));
        }
        if (tvDiscount != null) {
            tvDiscount.setText("-" + CurrencyFormatter.format(discount));
            if (layoutDiscount != null) {
                layoutDiscount.setVisibility(discount > 0 ? View.VISIBLE : View.GONE);
            }
        }
        if (tvTotal != null) {
            tvTotal.setText(CurrencyFormatter.format(total));
        }
    }

    private void setupPlaceOrder() {
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String name    = getText(etName);
        String phone   = getText(etPhone);
        String address = getText(etAddress);

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine payment method
        String method = "cod";
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_vnpay) method = "vnpay";
        else if (selectedId == R.id.rb_momo) method = "momo";

        // Build request
        CreateOrderRequest request = new CreateOrderRequest();
        DeliveryInfo delivery = new DeliveryInfo();
        delivery.setName(name);
        delivery.setPhone(phone);
        delivery.setAddress(address);
        request.setDelivery(delivery);

        // Build order items
        List<CreateOrderRequest.OrderItemRequest> items = new ArrayList<>();
        double subtotal = 0;
        for (CartItemEntity ci : cartItems) {
            CreateOrderRequest.OrderItemRequest oi = new CreateOrderRequest.OrderItemRequest();
            oi.setProductId(ci.getProductId());
            oi.setProductName(ci.getProductName());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProductPrice());
            oi.setSubtotal(ci.getLineTotal());
            items.add(oi);
            subtotal += ci.getLineTotal();
        }
        request.setItems(items);
        request.setSubtotal(subtotal);
        request.setShippingFee(30000); // default 30k

        String voucher = getText(etVoucher);
        if (!voucher.isEmpty()) {
            request.setVoucherCode(voucher);
            request.setDiscount(appliedDiscount);
        }

        String note = getText(etNote);
        if (!note.isEmpty()) request.setNote(note);

        double total = subtotal + 30000 - appliedDiscount;
        if (total < 0) total = 0;
        request.setTotalAmount(total);

        Map<String, String> paymentMap = new java.util.HashMap<>();
        paymentMap.put("method", method);
        request.setPayment(paymentMap);

        setLoading(true);
        String finalMethod = method;
        orderViewModel.createOrder(request).observe(this, result -> {
            setLoading(false);
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                String orderId = result.data.getId() != null ? result.data.getId() : result.data.getOrderId();
                trackPurchaseEvents(orderId);
                if ("cod".equals(finalMethod)) {
                    // Clear cart & show success
                    cartViewModel.clearCart();
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                    finish();
                } else if ("vnpay".equals(finalMethod)) {
                    // Get VNPay URL
                    orderViewModel.getVnpayUrl(orderId).observe(this, urlResult -> {
                        if (urlResult.status == AuthRepository.Result.Status.SUCCESS) {
                            Intent intent = new Intent(this, PaymentWebViewActivity.class);
                            intent.putExtra("payment_url", urlResult.data);
                            intent.putExtra("order_id", orderId);
                            startActivity(intent);
                        }
                    });
                } else if ("momo".equals(finalMethod)) {
                    setLoading(true);
                    orderViewModel.createMomoPayment(
                            orderId, result.data.getFinalAmount(), result.data.getUserId()).observe(this, momoResult -> {
                        setLoading(false);
                        if (momoResult.status == AuthRepository.Result.Status.SUCCESS && momoResult.data != null) {
                            Intent intent = new Intent(this, PaymentResultActivity.class);
                            intent.putExtra("payment_url", momoResult.data.getPayUrl());
                            intent.putExtra("deeplink", momoResult.data.getDeeplink());
                            intent.putExtra("order_id", orderId);
                            startActivity(intent);
                        } else if (momoResult.status == AuthRepository.Result.Status.ERROR) {
                            Toast.makeText(this, momoResult.message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void trackPurchaseEvents(String orderId) {
        if (cartItems == null || cartItems.isEmpty()) return;
        for (CartItemEntity item : cartItems) {
            java.util.Map<String, Object> meta = new HashMap<>();
            meta.put("quantity", item.getQuantity());
            meta.put("orderId", orderId);
            productViewModel.sendRecommendEvent(Constants.EVENT_PURCHASE, item.getProductId(), meta);
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        btnPlaceOrder.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
