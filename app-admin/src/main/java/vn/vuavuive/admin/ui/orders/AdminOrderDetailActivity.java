package vn.vuavuive.admin.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
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
import vn.vuavuive.shared.data.api.AdminUserApi;
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
    @Inject AdminUserApi adminUserApi;
    @Inject SessionManager sessionManager;

    private ActivityAdminOrderDetailBinding binding;
    private Order order;
    private User currentUser;
    private User selectedShipper;
    private final List<User> shippers = new ArrayList<>();
    private boolean isInitialSpinnerLoad = true;

    private static final List<String> STATUS_CODES = Arrays.asList(
            "pending", "confirmed", "preparing", "ready_for_pickup", "shipping", "in_transit", "delivered", "cancelled"
    );
    private static final List<String> STATUS_DISPLAY = Arrays.asList(
            "Chờ duyệt",
            "Đã xác nhận",
            "Đang chuẩn bị",
            "Sẵn sàng lấy hàng",
            "Đã gán shipper",
            "Đang giao",
            "Đã giao",
            "Đã hủy"
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
                    if (isFinishing() || isDestroyed()) return;
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
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(AdminOrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            return;
        }
        renderOrderDetails(orderId);
    }

    private void renderOrderDetails(String orderId) {
        if (order == null) {
            finish();
            return;
        }
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
                binding.layoutItemsContainer.addView(createOrderItemView(item));
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
        double subtotal = order.getSubtotal() > 0 ? order.getSubtotal() : (order.getTotalAmount() - order.getShippingFee() - order.getDiscount());
        binding.tvBreakdownSubtotal.setText(CurrencyFormatter.formatVnd(subtotal));
        binding.tvBreakdownShipping.setText("+ " + CurrencyFormatter.formatVnd(order.getShippingFee()));
        binding.tvBreakdownDiscount.setText("- " + CurrencyFormatter.formatVnd(order.getDiscount()));
        binding.tvBreakdownTotal.setText(CurrencyFormatter.formatVnd(order.getFinalAmount()));

        // 6. Bottom Actions: Mark Paid
        if (!isPaid && !"MOMO".equalsIgnoreCase(method) && !"audit".equalsIgnoreCase(currentUser.getRole())) {
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
                            renderOrderDetails(order.getId());
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

        // 8. Shipper assignment
        setupShipperAssignment(orderId);

        // 9. Return request handling
        setupReturnRequest();
    }

    private View createOrderItemView(OrderItem item) {
        int imageSize = (int) (64 * getResources().getDisplayMetrics().density);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 4, 0, (int) (10 * getResources().getDisplayMetrics().density));
        row.setLayoutParams(rowParams);

        ImageView image = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSize, imageSize);
        imageParams.setMargins(0, 0, (int) (12 * getResources().getDisplayMetrics().density), 0);
        image.setLayoutParams(imageParams);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        loadOrderItemImage(image, item);
        row.addView(image);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        String productName = item.getProductName() != null && !item.getProductName().isEmpty()
                ? item.getProductName()
                : (item.getProductId() != null ? item.getProductId() : "Sản phẩm");

        TextView name = new TextView(this);
        name.setText(productName + " (x" + item.getQuantity() + ")");
        name.setTextColor(getColor(R.color.text_primary));
        name.setTextSize(14f);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        texts.addView(name);

        TextView price = new TextView(this);
        price.setText("Đơn giá: " + CurrencyFormatter.formatVnd(item.getPrice())
                + " | Thành tiền: " + CurrencyFormatter.formatVnd(item.getLineTotal()));
        price.setTextColor(getColor(R.color.primary));
        price.setTextSize(12f);
        texts.addView(price);

        row.addView(texts);
        return row;
    }

    private void loadOrderItemImage(ImageView image, OrderItem item) {
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(image);
            return;
        }

        Glide.with(this)
                .load(R.drawable.ic_image)
                .into(image);

        String productId = item.getProductId();
        if (productId == null || productId.trim().isEmpty()) return;

        FirebaseDatabase.getInstance().getReference("products").child(productId).get()
                .addOnSuccessListener(snapshot -> {
                    String fallbackUrl = snapshot.child("image_url").getValue(String.class);
                    if (fallbackUrl == null || fallbackUrl.trim().isEmpty()) {
                        fallbackUrl = snapshot.child("imageUrl").getValue(String.class);
                    }
                    if (fallbackUrl != null && !fallbackUrl.trim().isEmpty()) {
                        Glide.with(this)
                                .load(fallbackUrl)
                                .placeholder(R.drawable.ic_image)
                                .error(R.drawable.ic_image)
                                .into(image);
                    }
                });
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
        if ("audit".equalsIgnoreCase(currentUser.getRole())) {
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
                                String errorMsg = "Không cập nhật được trạng thái (Code: " + response.code() + ")";
                                try {
                                    if (response.errorBody() != null) {
                                        errorMsg += " - " + response.errorBody().string();
                                    }
                                } catch (Exception ignored) {}
                                Toast.makeText(AdminOrderDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                            Toast.makeText(AdminOrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupShipperAssignment(String orderId) {
        boolean readOnly = "audit".equalsIgnoreCase(currentUser.getRole());
        binding.spinnerShipper.setEnabled(false);
        binding.btnAssignShipper.setEnabled(false);
        binding.btnAssignShipper.setText(readOnly ? "CHỈ XEM" : "GÁN SHIPPER");
        binding.tvCurrentShipper.setText(order.getShipperId() != null && !order.getShipperId().isEmpty()
                ? "Shipper hiện tại: " + order.getShipperId()
                : "Chưa gán shipper");

        if (readOnly) return;

        adminUserApi.getUsers(1, 200, null, "SHIPPER").enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call,
                                   @NonNull Response<ApiResponse<List<User>>> response) {
                shippers.clear();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    for (User shipper : response.body().getData()) {
                        if (shipper.isActive()) shippers.add(shipper);
                    }
                }

                List<String> labels = new ArrayList<>();
                int selectedIndex = 0;
                for (int i = 0; i < shippers.size(); i++) {
                    User shipper = shippers.get(i);
                    String label = (shipper.getName() != null ? shipper.getName() : shipper.getEmail())
                            + (shipper.getPhone() != null ? " - " + shipper.getPhone() : "");
                    labels.add(label);
                    if (shipper.getId() != null && shipper.getId().equals(order.getShipperId())) {
                        selectedIndex = i;
                        binding.tvCurrentShipper.setText("Shipper hiện tại: " + label);
                    }
                }

                if (labels.isEmpty()) labels.add("Không có shipper đang hoạt động");

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminOrderDetailActivity.this,
                        android.R.layout.simple_spinner_item, labels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerShipper.setAdapter(adapter);
                binding.spinnerShipper.setSelection(selectedIndex);

                selectedShipper = shippers.isEmpty() ? null : shippers.get(selectedIndex);
                boolean assignable = !shippers.isEmpty() && isAssignableStatus(order.getStatus());
                binding.spinnerShipper.setEnabled(!shippers.isEmpty());
                binding.btnAssignShipper.setEnabled(assignable);

                binding.spinnerShipper.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedShipper = position < shippers.size() ? shippers.get(position) : null;
                    }

                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                Toast.makeText(AdminOrderDetailActivity.this, "Không tải được danh sách shipper: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnAssignShipper.setOnClickListener(v -> {
            if (selectedShipper == null) {
                Toast.makeText(this, "Vui lòng chọn shipper", Toast.LENGTH_SHORT).show();
                return;
            }
            adminOrderApi.assignShipper(order.getId(), selectedShipper.getId()).enqueue(new Callback<java.util.Map<String, String>>() {
                @Override
                public void onResponse(@NonNull Call<java.util.Map<String, String>> call,
                                       @NonNull Response<java.util.Map<String, String>> response) {
                    java.util.Map<String, String> body = response.body();
                    if (response.isSuccessful() && body != null && !body.containsKey("error")) {
                        Toast.makeText(AdminOrderDetailActivity.this, "Đã gán shipper thành công", Toast.LENGTH_SHORT).show();
                        loadOrderDetails(orderId);
                    } else {
                        Toast.makeText(AdminOrderDetailActivity.this,
                                body != null && body.get("error") != null ? body.get("error") : "Không gán được shipper",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<java.util.Map<String, String>> call, @NonNull Throwable t) {
                    Toast.makeText(AdminOrderDetailActivity.this, "Lỗi gán shipper: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean isAssignableStatus(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return "pending".equals(s)
                || "pending_payment".equals(s)
                || "pending_approval".equals(s)
                || "confirmed".equals(s)
                || "preparing".equals(s)
                || "ready_for_pickup".equals(s)
                || "shipping".equals(s);
    }

    private void setupReturnRequest() {
        if ("return_requested".equals(order.getStatus())) {
            binding.layoutReturnReview.setVisibility(View.VISIBLE);
            if (order.getReturnRequest() != null) {
                binding.tvReturnReason.setText("Lý do: " + order.getReturnRequest().getReason());
            } else {
                binding.tvReturnReason.setText("Lý do: Khách hàng yêu cầu hoàn tiền hàng lỗi");
            }

            if ("audit".equalsIgnoreCase(currentUser.getRole())) {
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
                    Toast.makeText(AdminOrderDetailActivity.this, "Không cập nhật được trạng thái trả hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                Toast.makeText(AdminOrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
