package vn.vuavuive.customer.ui.checkout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.auth.LoginActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.data.dto.CreateMomoPaymentResponse;
import vn.vuavuive.shared.data.dto.CreateZaloPayPaymentResponse;
import vn.vuavuive.shared.data.dto.DeliveryInfo;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.Constants;
import vn.vuavuive.shared.util.CurrencyFormatter;

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
    private boolean cartLoaded = false;
    private List<CartItemEntity> cartItems = new ArrayList<>();

    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String address = result.getData().getStringExtra(MapPickerActivity.EXTRA_ADDRESS);
                    if (address != null && !address.isEmpty() && etAddress != null) {
                        etAddress.setText(address);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
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
        etName = findViewById(R.id.et_receiver_name);
        etPhone = findViewById(R.id.et_receiver_phone);
        etAddress = findViewById(R.id.et_delivery_address);
        etVoucher = findViewById(R.id.et_voucher_code);
        etNote = findViewById(R.id.et_note);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        btnApplyVoucher = findViewById(R.id.btn_apply_voucher);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        progressBar = findViewById(R.id.progress_bar);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvDiscount = findViewById(R.id.tv_discount);
        tvTotal = findViewById(R.id.tv_total);
        layoutDiscount = findViewById(R.id.layout_discount);

        if (btnApplyVoucher != null) {
            btnApplyVoucher.setOnClickListener(v -> applyVoucherCode());
        }

        TextInputLayout tilAddress = findViewById(R.id.til_delivery_address);
        if (tilAddress != null) {
            tilAddress.setEndIconOnClickListener(v ->
                    mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class)));
        }
    }

    private void observeCart() {
        if (btnPlaceOrder != null) btnPlaceOrder.setEnabled(false);

        cartViewModel.getCartItems().observe(this, items -> {
            cartItems = items != null ? items : new ArrayList<>();
            cartLoaded = true;
            updatePriceSummary();
            if (btnPlaceOrder != null && progressBar != null
                    && progressBar.getVisibility() != View.VISIBLE) {
                btnPlaceOrder.setEnabled(true);
            }
        });
    }

    private void setupPlaceOrder() {
        if (btnPlaceOrder != null) {
            btnPlaceOrder.setOnClickListener(v -> placeOrder());
        }
    }

    private void applyVoucherCode() {
        String code = getText(etVoucher);
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã", Toast.LENGTH_SHORT).show();
            return;
        }

        // Voucher client-side demo only; server rules should own final validation later.
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

    private void placeOrder() {
        String name = getText(etName);
        String phone = getText(etPhone);
        String address = getText(etAddress);

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches("0\\d{9}")) {
            Toast.makeText(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address.length() < 10) {
            Toast.makeText(this, "Địa chỉ giao hàng quá ngắn", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cartLoaded || cartItems.isEmpty()) {
            setLoading(true);
            new Thread(() -> {
                List<CartItemEntity> syncItems = cartViewModel.getCartItemsSync();
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    setLoading(false);
                    if (syncItems == null || syncItems.isEmpty()) {
                        Toast.makeText(this, "Giỏ hàng trống, vui lòng thêm sản phẩm trước", Toast.LENGTH_SHORT).show();
                    } else {
                        cartItems = new ArrayList<>(syncItems);
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

    private void doPlaceOrder(String name, String phone, String address) {
        String method = Constants.PAYMENT_COD;
        int selectedId = rgPaymentMethod != null ? rgPaymentMethod.getCheckedRadioButtonId() : View.NO_ID;
        if (selectedId == R.id.rb_momo) method = Constants.PAYMENT_MOMO;
        else if (selectedId == R.id.rb_zalopay) method = Constants.PAYMENT_ZALOPAY;

        CreateOrderRequest request = buildOrderRequest(name, phone, address, method);
        double localTotal = request.getTotalAmount();
        String finalMethod = method;

        setLoading(true);
        LiveData<AuthRepository.Result<Order>> liveData = orderViewModel.createOrder(request);
        liveData.observe(this, new Observer<AuthRepository.Result<Order>>() {
            @Override
            public void onChanged(AuthRepository.Result<Order> result) {
                if (result == null || result.status == AuthRepository.Result.Status.LOADING) return;
                liveData.removeObserver(this);
                setLoading(false);

                if (result.status == AuthRepository.Result.Status.ERROR) {
                    Toast.makeText(CheckoutActivity.this, result.message, Toast.LENGTH_LONG).show();
                    return;
                }
                if (result.data == null) return;

                Order order = result.data;
                String orderId = order.getId() != null ? order.getId() : order.getOrderId();
                if (orderId == null || orderId.isEmpty()) {
                    Toast.makeText(CheckoutActivity.this, "Không lấy được mã đơn hàng", Toast.LENGTH_LONG).show();
                    return;
                }

                double payableAmount = order.getFinalAmount() > 0 ? order.getFinalAmount() : localTotal;
                trackPurchaseEvents(orderId);

                if (Constants.PAYMENT_COD.equals(finalMethod)) {
                    cartViewModel.clearCart();
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                    finish();
                } else if (order.getPaymentUrl() != null && !order.getPaymentUrl().isEmpty()) {
                    openPaymentResult(orderId, payableAmount, finalMethod, order.getPaymentUrl(), null);
                } else if (Constants.PAYMENT_MOMO.equals(finalMethod)) {
                    createMomoPayment(orderId, payableAmount, order.getUserId());
                } else if (Constants.PAYMENT_ZALOPAY.equals(finalMethod)) {
                    createZaloPayPayment(orderId, payableAmount);
                }
            }
        });
    }

    private CreateOrderRequest buildOrderRequest(String name, String phone, String address, String method) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setDelivery(new DeliveryInfo(name, phone, address, getText(etNote)));

        List<CreateOrderRequest.OrderItemRequest> items = new ArrayList<>();
        double subtotal = 0;
        for (CartItemEntity ci : cartItems) {
            if (ci == null || ci.getProductId() == null || ci.getProductId().isEmpty()) continue;
            CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
            item.setProductId(ci.getProductId());
            item.setProductName(ci.getProductName());
            item.setQuantity(ci.getQuantity());
            item.setPrice(ci.getProductPrice());
            item.setSubtotal(ci.getLineTotal());
            item.setUnit(ci.getProductUnit());
            item.setImageUrl(ci.getProductImageUrl());
            items.add(item);
            subtotal += ci.getLineTotal();
        }

        request.setItems(items);
        request.setSubtotal(subtotal);
        request.setShippingFee(30_000);
        request.setDiscount(appliedDiscount);
        request.setTotalAmount(Math.max(subtotal + 30_000 - appliedDiscount, 0));

        String voucher = getText(etVoucher);
        if (!voucher.isEmpty()) request.setVoucherCode(voucher);

        String note = getText(etNote);
        if (!note.isEmpty()) request.setNote(note);

        Map<String, String> payment = new HashMap<>();
        payment.put("method", method);
        request.setPayment(payment);
        request.setPaymentMethod(method);
        return request;
    }

    private void createMomoPayment(String orderId, double amount, String userId) {
        setLoading(true);
        LiveData<AuthRepository.Result<CreateMomoPaymentResponse>> liveData =
                orderViewModel.createMomoPayment(orderId, amount, userId);
        liveData.observe(this, new Observer<AuthRepository.Result<CreateMomoPaymentResponse>>() {
            @Override
            public void onChanged(AuthRepository.Result<CreateMomoPaymentResponse> result) {
                if (result == null || result.status == AuthRepository.Result.Status.LOADING) return;
                liveData.removeObserver(this);
                setLoading(false);

                if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                    openPaymentResult(orderId, amount, "MOMO", result.data.getPayUrl(), result.data.getDeeplink());
                } else if (result.status == AuthRepository.Result.Status.ERROR) {
                    Toast.makeText(CheckoutActivity.this, result.message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createZaloPayPayment(String orderId, double amount) {
        setLoading(true);
        LiveData<AuthRepository.Result<CreateZaloPayPaymentResponse>> liveData =
                orderViewModel.createZaloPayPayment(orderId, amount, "Thanh toan don hang Vua Vui Ve: " + orderId);
        liveData.observe(this, new Observer<AuthRepository.Result<CreateZaloPayPaymentResponse>>() {
            @Override
            public void onChanged(AuthRepository.Result<CreateZaloPayPaymentResponse> result) {
                if (result == null || result.status == AuthRepository.Result.Status.LOADING) return;
                liveData.removeObserver(this);
                setLoading(false);

                if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                    openPaymentResult(orderId, amount, "ZALOPAY", result.data.getOrderUrl(), result.data.getZpTransToken());
                } else if (result.status == AuthRepository.Result.Status.ERROR) {
                    Toast.makeText(CheckoutActivity.this, result.message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void openPaymentResult(String orderId, double amount, String provider, String paymentUrl, String deeplink) {
        Intent intent = new Intent(CheckoutActivity.this, PaymentResultActivity.class);
        intent.putExtra("payment_url", paymentUrl);
        intent.putExtra("deeplink", deeplink);
        intent.putExtra("order_id", orderId);
        intent.putExtra("order_total", amount);
        intent.putExtra("provider", provider != null ? provider.toUpperCase() : "");
        startActivity(intent);
    }

    private double getSubtotal() {
        double total = 0;
        if (cartItems == null) return total;
        for (CartItemEntity item : cartItems) {
            if (item != null) total += item.getLineTotal();
        }
        return total;
    }

    private void updatePriceSummary() {
        double subtotal = getSubtotal();
        double shippingFee = 30_000;
        double discount = appliedDiscount;
        double total = Math.max(subtotal + shippingFee - discount, 0);

        if (tvSubtotal != null) tvSubtotal.setText(CurrencyFormatter.format(subtotal));
        if (tvShippingFee != null) tvShippingFee.setText(CurrencyFormatter.format(shippingFee));
        if (tvDiscount != null) tvDiscount.setText("-" + CurrencyFormatter.format(discount));
        if (layoutDiscount != null) layoutDiscount.setVisibility(discount > 0 ? View.VISIBLE : View.GONE);
        if (tvTotal != null) tvTotal.setText(CurrencyFormatter.format(total));
    }

    private void trackPurchaseEvents(String orderId) {
        if (cartItems == null || cartItems.isEmpty()) return;
        for (CartItemEntity item : cartItems) {
            if (item == null || item.getProductId() == null || item.getProductId().isEmpty()) continue;
            Map<String, Object> meta = new HashMap<>();
            meta.put("quantity", item.getQuantity());
            meta.put("orderId", orderId);
            productViewModel.sendRecommendEvent(Constants.EVENT_PURCHASE, item.getProductId(), meta);
        }
    }

    private String getText(TextInputEditText editText) {
        return editText != null && editText.getText() != null
                ? editText.getText().toString().trim()
                : "";
    }

    private void setLoading(boolean loading) {
        if (btnPlaceOrder != null) btnPlaceOrder.setEnabled(!loading);
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
