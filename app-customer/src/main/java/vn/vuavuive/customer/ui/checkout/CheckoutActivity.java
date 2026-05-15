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
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
import vn.vuavuive.shared.data.dto.DeliveryInfo;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest;
import vn.vuavuive.shared.data.local.CartItemEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AndroidEntryPoint
public class CheckoutActivity extends AppCompatActivity {

    private OrderViewModel orderViewModel;
    private CartViewModel cartViewModel;

    private TextInputEditText etName, etPhone, etAddress, etVoucher, etNote;
    private RadioGroup rgPaymentMethod;
    private MaterialButton btnPlaceOrder;
    private ProgressBar progressBar;

    private List<CartItemEntity> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        cartViewModel  = new ViewModelProvider(this).get(CartViewModel.class);

        initViews();
        observeCart();
        setupPlaceOrder();
    }

    private void initViews() {
        etName      = findViewById(R.id.et_receiver_name);
        etPhone     = findViewById(R.id.et_receiver_phone);
        etAddress   = findViewById(R.id.et_delivery_address);
        etVoucher   = findViewById(R.id.et_voucher_code);
        etNote      = findViewById(R.id.et_note);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        progressBar  = findViewById(R.id.progress_bar);
    }

    private void observeCart() {
        cartViewModel.getCartItems().observe(this, items -> {
            cartItems = items != null ? items : new ArrayList<>();
        });
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
        if (!voucher.isEmpty()) request.setVoucherCode(voucher);

        String note = getText(etNote);
        if (!note.isEmpty()) request.setNote(note);

        double total = subtotal + 30000 - request.getDiscount();
        request.setTotalAmount(total);

        Map<String, String> paymentMap = new java.util.HashMap<>();
        paymentMap.put("method", method);
        request.setPayment(paymentMap);

        setLoading(true);
        String finalMethod = method;
        orderViewModel.createOrder(request).observe(this, result -> {
            setLoading(false);
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                String orderId = result.data.getId();
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
                    orderViewModel.getMomoUrl(orderId).observe(this, urlResult -> {
                        if (urlResult.status == AuthRepository.Result.Status.SUCCESS) {
                            Intent intent = new Intent(this, PaymentWebViewActivity.class);
                            intent.putExtra("payment_url", urlResult.data);
                            intent.putExtra("order_id", orderId);
                            startActivity(intent);
                        }
                    });
                }
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        btnPlaceOrder.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
