package vn.vuavuive.admin.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.databinding.ActivityAdminOrderDetailBinding;
import vn.vuavuive.shared.data.api.AdminOrderApi;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.OrderItem;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.CurrencyFormatter;
import vn.vuavuive.shared.util.SessionManager;

@AndroidEntryPoint
public class AdminOrderDetailActivity extends AppCompatActivity {

    @Inject OrderApi orderApi;
    @Inject AdminOrderApi adminOrderApi;
    @Inject SessionManager sessionManager;

    private ActivityAdminOrderDetailBinding binding;
    private Order order;
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

        currentUser = sessionManager.getUser();
        if (currentUser != null && currentUser.getRole() != null) {
            currentUser.setRole(currentUser.getRole().toLowerCase());
        }
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
        if (orderId != null) {
            orderApi.getOrderDetail(orderId).enqueue(new Callback<ApiResponse<Order>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Order>> call,
                                       @NonNull Response<ApiResponse<Order>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                            && response.body().getData() != null) {
                        order = response.body().getData();
                        renderOrderDetails(orderId);
                    } else {
                        Toast.makeText(AdminOrderDetailActivity.this, "Đơn hàng không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                    Toast.makeText(AdminOrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            return;
        }
        renderOrderDetails(orderId);
    }

    private void renderOrderDetails(String orderId) {
        // 1. Core Header & IDs
        binding.tvOrderIdTitle.setText("Mã đơn: " + (order.getOrderId() != null ? order.getOrderId() : order.getId()));
        binding.tvOrderDateVal.setText(order.getCreatedAt() != null ? order.getCreatedAt().replace("T", " ").replace("Z", "") : "");

        // 2. Customer & Address Information
        if (order.getRecipientName() != null || order.getRecipientPhone() != null || order.getRecipientAddress() != null) {
            binding.tvDetailCustomer.setText("Khách hàng: " + (order.getRecipientName() != null ? order.getRecipientName() : "N/A"));
            binding.tvDetailPhone.setText("SĐT: " + (order.getRecipientPhone() != null ? order.getRecipientPhone() : "N/A"));
            binding.tvDetailAddress.setText("Địa chỉ: " + (order.getRecipientAddress() != null ? order.getRecipientAddress() : "N/A"));
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
            String paymentStatus = order.getPayment().getStatus();
            if ("paid".equalsIgnoreCase(paymentStatus) || order.isPaid()) {
                statusStr = "Paid";
                isPaid = true;
            } else if ("failed".equalsIgnoreCase(paymentStatus)) {
                statusStr = "Failed";
            } else if ("pending".equalsIgnoreCase(paymentStatus)) {
                statusStr = "Pending";
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
        binding.tvBreakdownTotal.setText(CurrencyFormatter.formatVnd(order.getFinalAmount()));

        // 6. Bottom Actions: Mark Paid
        if (!isPaid && !"MOMO".equalsIgnoreCase(method) && !"audit".equals(currentUser.getRole())) {
            binding.btnMarkPaid.setVisibility(View.VISIBLE);
            binding.btnMarkPaid.setOnClickListener(v -> {
                adminOrderApi.markPaid(order.getId()).enqueue(new Callback<ApiResponse<Order>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Order>> call,
                                           @NonNull Response<ApiResponse<Order>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                                && response.body().getData() != null) {
                            order = response.body().getData();
                            Toast.makeText(AdminOrderDetailActivity.this, "Đã cập nhật trạng thái: ĐÃ THANH TOÁN", Toast.LENGTH_SHORT).show();
                            renderOrderDetails(orderId);
                        } else {
                            Toast.makeText(AdminOrderDetailActivity.this, "Không cập nhật được thanh toán", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                        Toast.makeText(AdminOrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        int currentIndex = STATUS_CODES.indexOf(order.getStatus() != null ? order.getStatus().toLowerCase() : "");
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
                if (!newStatus.equalsIgnoreCase(order.getStatus())) {
                    java.util.Map<String, String> body = new java.util.HashMap<>();
                    body.put("status", newStatus);
                    body.put("updatedBy", currentUser.getName() != null ? currentUser.getName() : "Admin");
                    adminOrderApi.updateOrderStatus(order.getId(), body).enqueue(new Callback<ApiResponse<Order>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<Order>> call,
                                               @NonNull Response<ApiResponse<Order>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                                    && response.body().getData() != null) {
                                order = response.body().getData();
                                Toast.makeText(AdminOrderDetailActivity.this, "Đã chuyển trạng thái sang: " + newStatus.toUpperCase(), Toast.LENGTH_SHORT).show();
                                renderOrderDetails(order.getId());
                            } else {
                                Toast.makeText(AdminOrderDetailActivity.this, "Không cập nhật được trạng thái", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                            Toast.makeText(AdminOrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
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
                updateReturnStatus("returned");
                Toast.makeText(this, "Đã CHẤP THUẬN yêu cầu trả hàng", Toast.LENGTH_SHORT).show();
            });

            binding.btnRejectReturn.setOnClickListener(v -> {
                updateReturnStatus("delivered");
                Toast.makeText(this, "Đã TỪ CHỐI yêu cầu trả hàng", Toast.LENGTH_SHORT).show();
            });
        } else {
            binding.layoutReturnReview.setVisibility(View.GONE);
        }
    }

    private void updateReturnStatus(String status) {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", status);
        body.put("updatedBy", currentUser.getName() != null ? currentUser.getName() : "Admin");
        adminOrderApi.updateOrderStatus(order.getId(), body).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call,
                                   @NonNull Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    order = response.body().getData();
                    renderOrderDetails(order.getId());
                } else {
                    Toast.makeText(AdminOrderDetailActivity.this, "Khong cap nhat duoc tra hang", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                Toast.makeText(AdminOrderDetailActivity.this, "Loi ket noi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
