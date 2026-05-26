package vn.vuavuive.admin.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.ActivityAdminOrderDetailBinding;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.OrderItem;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.CurrencyFormatter;

public class AdminOrderDetailActivity extends AppCompatActivity {

    private ActivityAdminOrderDetailBinding binding;
    private Order order;
    private MockRepository repo;
    private User currentUser;
    private boolean isInitialSpinnerLoad = true;

    private static final List<String> STATUS_CODES = Arrays.asList(
            "pending", "confirmed", "shipping", "delivered", "cancelled"
    );
    private static final List<String> STATUS_DISPLAY = Arrays.asList(
            "Chờ duyệt (Pending)", 
            "Đã xác nhận (Confirmed)", 
            "Đang giao hàng (Shipping)", 
            "Đã giao hàng (Delivered)", 
            "Đã hủy đơn (Cancelled)"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repo = MockRepository.getInstance();
        currentUser = repo.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderDetails(orderId);

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadOrderDetails(String orderId) {
        order = null;
        for (Order o : repo.getOrders()) {
            if (o.getId().equals(orderId)) {
                order = o;
                break;
            }
        }

        if (order == null) {
            Toast.makeText(this, "Đơn hàng không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 1. Core Header & IDs
        binding.tvOrderIdTitle.setText("Mã đơn: " + (order.getOrderId() != null ? order.getOrderId() : order.getId()));
        binding.tvOrderDateVal.setText(order.getCreatedAt() != null ? order.getCreatedAt().replace("T", " ").replace("Z", "") : "");

        // 2. Customer & Address Information
        if (order.getDelivery() != null) {
            binding.tvDetailCustomer.setText("Khách hàng: " + order.getDelivery().getName());
            binding.tvDetailPhone.setText("SĐT: " + order.getDelivery().getPhone());
            binding.tvDetailAddress.setText("Địa chỉ: " + order.getDelivery().getAddress());
        } else {
            binding.tvDetailCustomer.setText("Khách hàng: N/A");
            binding.tvDetailPhone.setText("SĐT: N/A");
            binding.tvDetailAddress.setText("Địa chỉ: N/A");
        }

        // 3. Dynamic Products list
        binding.layoutItemsContainer.removeAllViews();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                View itemView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, binding.layoutItemsContainer, false);
                TextView text1 = itemView.findViewById(android.R.id.text1);
                TextView text2 = itemView.findViewById(android.R.id.text2);

                text1.setText(item.getProductName() + " (x" + item.getQuantity() + ")");
                text1.setTextColor(getColor(R.color.text_primary));
                text1.setTextSize(14f);

                double price = item.getPrice();
                text2.setText("Đơn giá: " + CurrencyFormatter.formatVnd(price) + " | Thành tiền: " + CurrencyFormatter.formatVnd(item.getLineTotal()));
                text2.setTextColor(getColor(R.color.primary));
                text2.setTextSize(12f);

                // Add small margins
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 4, 0, 8);
                itemView.setLayoutParams(params);

                binding.layoutItemsContainer.addView(itemView);
            }
        }

        // 4. Payment Details
        String method = "COD";
        String statusStr = "Chưa thanh toán";
        boolean isPaid = false;

        if (order.getPayment() != null) {
            if (order.getPayment().getMethod() != null) {
                method = order.getPayment().getMethod().toUpperCase();
            }
            if ("paid".equalsIgnoreCase(order.getPayment().getStatus()) || order.isPaid()) {
                statusStr = "Đã thanh toán";
                isPaid = true;
            }
            if (order.getPayment().getTransactionId() != null) {
                binding.layoutTxnId.setVisibility(View.VISIBLE);
                binding.tvTxnIdVal.setText(order.getPayment().getTransactionId());
            } else {
                binding.layoutTxnId.setVisibility(View.GONE);
            }
        } else {
            binding.layoutTxnId.setVisibility(View.GONE);
        }

        binding.tvPaymentMethodVal.setText(method);
        binding.tvPaymentStatusVal.setText(statusStr);
        binding.tvPaymentStatusVal.setTextColor(isPaid ? getColor(R.color.success) : getColor(R.color.error));

        // 5. Price Breakdown
        double subtotal = order.getSubtotal() > 0 ? order.getSubtotal() : (order.getTotalAmount() - order.getShippingFee() + order.getDiscount());
        binding.tvBreakdownSubtotal.setText(CurrencyFormatter.formatVnd(subtotal));
        binding.tvBreakdownShipping.setText("+ " + CurrencyFormatter.formatVnd(order.getShippingFee()));
        binding.tvBreakdownDiscount.setText("- " + CurrencyFormatter.formatVnd(order.getDiscount()));
        binding.tvBreakdownTotal.setText(CurrencyFormatter.formatVnd(order.getTotalAmount()));

        // 6. Bottom Actions: Mark Paid
        if (!isPaid && !"audit".equals(currentUser.getRole())) {
            binding.btnMarkPaid.setVisibility(View.VISIBLE);
            binding.btnMarkPaid.setOnClickListener(v -> {
                repo.markPaid(order.getId());
                Toast.makeText(this, "Đã cập nhật trạng thái: ĐÃ THANH TOÁN", Toast.LENGTH_SHORT).show();
                loadOrderDetails(orderId); // reload view
            });
        } else {
            binding.btnMarkPaid.setVisibility(View.GONE);
        }

        // 7. Status Spinner configuration
        setupStatusSpinner();

        // 8. Return request handling
        setupReturnRequest();
    }

    private void setupStatusSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, STATUS_DISPLAY);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerOrderStatus.setAdapter(spinnerAdapter);

        // Find current status index
        int currentIndex = STATUS_CODES.indexOf(order.getStatus());
        if (currentIndex >= 0) {
            isInitialSpinnerLoad = true;
            binding.spinnerOrderStatus.setSelection(currentIndex);
        }

        // Restricted role check (Audit role is read-only)
        if ("audit".equals(currentUser.getRole())) {
            binding.spinnerOrderStatus.setEnabled(false);
            return;
        }

        binding.spinnerOrderStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialSpinnerLoad) {
                    isInitialSpinnerLoad = false;
                    return;
                }
                String newStatus = STATUS_CODES.get(position);
                if (!newStatus.equals(order.getStatus())) {
                    repo.updateOrderStatus(order.getId(), newStatus);
                    Toast.makeText(AdminOrderDetailActivity.this, "Đã chuyển trạng thái sang: " + newStatus.toUpperCase(), Toast.LENGTH_SHORT).show();
                    loadOrderDetails(order.getId()); // Refresh items and shipment states!
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupReturnRequest() {
        if ("return_requested".equals(order.getStatus())) {
            binding.layoutReturnReview.setVisibility(View.VISIBLE);
            if (order.getReturnRequest() != null) {
                binding.tvReturnReason.setText("Lý do: " + order.getReturnRequest().getReason());
            } else {
                binding.tvReturnReason.setText("Lý do: Khách hàng yêu cầu hoàn tiền hàng lỗi");
            }

            if ("audit".equals(currentUser.getRole())) {
                binding.btnApproveReturn.setEnabled(false);
                binding.btnRejectReturn.setEnabled(false);
                return;
            }

            binding.btnApproveReturn.setOnClickListener(v -> {
                repo.approveReturn(order.getId(), true, "Chấp nhận trả hàng hoàn tiền từ admin");
                Toast.makeText(this, "Đã CHẤP THUẬN yêu cầu trả hàng", Toast.LENGTH_SHORT).show();
                loadOrderDetails(order.getId());
            });

            binding.btnRejectReturn.setOnClickListener(v -> {
                repo.approveReturn(order.getId(), false, "Từ chối trả hàng từ admin");
                Toast.makeText(this, "Đã TỪ CHỐI yêu cầu trả hàng", Toast.LENGTH_SHORT).show();
                loadOrderDetails(order.getId());
            });
        } else {
            binding.layoutReturnReview.setVisibility(View.GONE);
        }
    }
}
