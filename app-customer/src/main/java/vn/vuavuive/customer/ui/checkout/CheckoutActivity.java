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

    /**
     * Guard flags cho trường hợp race condition với giỏ hàng.
     * cartLoaded  = true sau khi LiveData đã trả về lần đầu.
     * cartItems   = các items được trả về (có thể empty nếu giỏ hàng thực sự trống).
     */
    private boolean cartLoaded = false;
    private List<CartItemEntity> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        orderViewModel   = new ViewModelProvider(this).get(OrderViewModel.class);
        cartViewModel    = new ViewModelProvider(this).get(CartViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        authViewModel    = new ViewModelProvider(this).get(AuthViewModel.class);

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

    // ── Views ─────────────────────────────────────────────────────────────────

    private void initViews() {
        etName          = findViewById(R.id.et_receiver_name);
        etPhone         = findViewById(R.id.et_receiver_phone);
        etAddress       = findViewById(R.id.et_delivery_address);
        etVoucher       = findViewById(R.id.et_voucher_code);
        btnApplyVoucher = findViewById(R.id.btn_apply_voucher);
        etNote          = findViewById(R.id.et_note);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        btnPlaceOrder   = findViewById(R.id.btn_place_order);
        progressBar     = findViewById(R.id.progress_bar);
        tvSubtotal      = findViewById(R.id.tv_subtotal);
        tvShippingFee   = findViewById(R.id.tv_shipping_fee);
        tvDiscount      = findViewById(R.id.tv_discount);
        tvTotal         = findViewById(R.id.tv_total);
        layoutDiscount  = findViewById(R.id.layout_discount);

        if (btnApplyVoucher != null) {
            btnApplyVoucher.setOnClickListener(v -> applyVoucherCode());
        }
    }

    // ── Cart observation ──────────────────────────────────────────────────────

    private void observeCart() {
        // Disable button cho đến khi Firebase LiveData có dữ liệu — tránh race condition
        // khi người dùng bấm "Đặt hàng" trước khi LiveData được gọi lần đầu.
        if (btnPlaceOrder != null) btnPlaceOrder.setEnabled(false);

        cartViewModel.getCartItems().observe(this, items -> {
            cartItems  = items != null ? items : new ArrayList<>();
            cartLoaded = true;
            updatePriceSummary();
            // Re-enable once we have real data (and loading spinner is not active)
            if (btnPlaceOrder != null && progressBar != null
                    && progressBar.getVisibility() != View.VISIBLE) {
                btnPlaceOrder.setEnabled(true);
            }
        });
    }

    // ── Price summary ─────────────────────────────────────────────────────────

    private double getSubtotal() {
        double total = 0;
        for (CartItemEntity ci : cartItems) total += ci.getLineTotal();
        return total;
    }

    private void updatePriceSummary() {
        double subtotal    = getSubtotal();
        double shippingFee = 30_000;
        double discount    = appliedDiscount;
        double total       = Math.max(subtotal + shippingFee - discount, 0);

        if (tvSubtotal   != null) tvSubtotal.setText(CurrencyFormatter.format(subtotal));
        if (tvShippingFee != null) tvShippingFee.setText(CurrencyFormatter.format(shippingFee));
        if (tvDiscount   != null) {
            tvDiscount.setText("-" + CurrencyFormatter.format(discount));
            if (layoutDiscount != null)
                layoutDiscount.setVisibility(discount > 0 ? View.VISIBLE : View.GONE);
        }
        if (tvTotal != null) tvTotal.setText(CurrencyFormatter.format(total));
    }

    // ── Voucher ───────────────────────────────────────────────────────────────

    private void applyVoucherCode() {
        String code = getText(etVoucher);
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("VUAVUIVE".equalsIgnoreCase(code)) {
            appliedDiscount = getSubtotal() * 0.15;
            Toast.makeText(this, "Áp dụng mã thành công! Giảm 15%", Toast.LENGTH_SHORT).show();
        } else if ("FREESHIP24".equalsIgnoreCase(code) || "FREESHIP".equalsIgnoreCase(code)) {
            appliedDiscount = 30_000;
            Toast.makeText(this, "Áp dụng mã thành công! Miễn phí ship", Toast.LENGTH_SHORT).show();
        } else {
            appliedDiscount = 0;
            Toast.makeText(this, "Mã không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
        }
        updatePriceSummary();
    }

    // ── Place-order ───────────────────────────────────────────────────────────

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

        // ── Belt-and-suspenders guard ────────────────────────────────────────
        // cartLoaded should already be true (button was disabled until LiveData fired),
        // but defend against rare re-entrancy / orientation-change edge cases.
        if (!cartLoaded || cartItems.isEmpty()) {
            setLoading(true);
            new Thread(() -> {
                List<CartItemEntity> syncItems = cartViewModel.getCartItemsSync();
                runOnUiThread(() -> {
                    setLoading(false);
                    if (syncItems == null || syncItems.isEmpty()) {
                        Toast.makeText(this,
                                "Giỏ hàng trống — vui lòng thêm sản phẩm trước",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        cartItems  = new ArrayList<>(syncItems);
                        cartLoaded = true;
                        updatePriceSummary();
                        doPlaceOrder(name, phone, address);
                    }
                });
            }).start();
            return;
        }

        doPlaceOrder(name, phone, address);
    }

    /**
     * The actual ordering logic — only called after {@code cartItems} is guaranteed non-empty.
     */
    private void doPlaceOrder(String name, String phone, String address) {
        // Determine payment method
        String method = Constants.PAYMENT_COD;
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if      (selectedId == R.id.rb_momo)    method = Constants.PAYMENT_MOMO;
        else if (selectedId == R.id.rb_zalopay) method = Constants.PAYMENT_ZALOPAY;

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
            oi.setUnit(ci.getProductUnit());
            oi.setImageUrl(ci.getProductImageUrl());
            items.add(oi);
            subtotal += ci.getLineTotal();
        }
        request.setItems(items);
        request.setSubtotal(subtotal);
        request.setShippingFee(30_000);

        String voucher = getText(etVoucher);
        if (!voucher.isEmpty()) {
            request.setVoucherCode(voucher);
            request.setDiscount(appliedDiscount);
        }
        String note = getText(etNote);
        if (!note.isEmpty()) request.setNote(note);

        double total = Math.max(subtotal + 30_000 - appliedDiscount, 0);
        request.setTotalAmount(total);

        Map<String, String> paymentMap = new HashMap<>();
        paymentMap.put("method", method);
        request.setPayment(paymentMap);

        setLoading(true);
        String finalMethod = method;
        orderViewModel.createOrder(request).observe(this, result -> {
            setLoading(false);
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                String orderId = result.data.getId() != null ? result.data.getId() : result.data.getOrderId();
                trackPurchaseEvents(orderId);

                if (Constants.PAYMENT_COD.equals(finalMethod)) {
                    cartViewModel.clearCart();
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                    finish();

                } else if (Constants.PAYMENT_MOMO.equals(finalMethod)) {
                    setLoading(true);
                    orderViewModel.createMomoPayment(
                            orderId, result.data.getFinalAmount(), result.data.getUserId())
                            .observe(this, momoResult -> {
                                setLoading(false);
                                if (momoResult.status == AuthRepository.Result.Status.SUCCESS
                                        && momoResult.data != null) {
                                    Intent intent = new Intent(this, PaymentResultActivity.class);
                                    intent.putExtra("payment_url", momoResult.data.getPayUrl());
                                    intent.putExtra("deeplink",    momoResult.data.getDeeplink());
                                    intent.putExtra("order_id",    orderId);
                                    intent.putExtra("order_total", result.data.getFinalAmount());
                                    intent.putExtra("provider",    "MOMO");
                                    startActivity(intent);
                                } else if (momoResult.status == AuthRepository.Result.Status.ERROR) {
                                    Toast.makeText(this, momoResult.message, Toast.LENGTH_LONG).show();
                                }
                            });

                } else if (Constants.PAYMENT_ZALOPAY.equals(finalMethod)) {
                    setLoading(true);
                    orderViewModel.createZaloPayPayment(
                            orderId,
                            result.data.getFinalAmount(),
                            "Thanh toan don hang Vua Vui Ve: " + orderId)
                            .observe(this, zaloPayResult -> {
                                setLoading(false);
                                if (zaloPayResult.status == AuthRepository.Result.Status.SUCCESS
                                        && zaloPayResult.data != null) {
                                    Intent intent = new Intent(this, PaymentResultActivity.class);
                                    intent.putExtra("payment_url", zaloPayResult.data.getOrderUrl());
                                    intent.putExtra("deeplink",    zaloPayResult.data.getZpTransToken());
                                    intent.putExtra("order_id",    orderId);
                                    intent.putExtra("order_total", result.data.getFinalAmount());
                                    intent.putExtra("provider",    "ZALOPAY");
                                    startActivity(intent);
                                } else if (zaloPayResult.status == AuthRepository.Result.Status.ERROR) {
                                    Toast.makeText(this, zaloPayResult.message, Toast.LENGTH_LONG).show();
                                }
                            });
                }

            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void trackPurchaseEvents(String orderId) {
        if (cartItems == null || cartItems.isEmpty()) return;
        for (CartItemEntity item : cartItems) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("quantity", item.getQuantity());
            meta.put("orderId",  orderId);
            productViewModel.sendRecommendEvent(Constants.EVENT_PURCHASE, item.getProductId(), meta);
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        if (btnPlaceOrder != null) btnPlaceOrder.setEnabled(!loading);
        if (progressBar   != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
