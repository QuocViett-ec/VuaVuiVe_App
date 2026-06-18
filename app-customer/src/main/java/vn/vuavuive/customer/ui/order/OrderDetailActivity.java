package vn.vuavuive.customer.ui.order;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
import vn.vuavuive.customer.ui.review.ReviewBottomSheetDialogFragment;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.OrderItem;
import vn.vuavuive.shared.util.CurrencyFormatter;

@AndroidEntryPoint
public class OrderDetailActivity extends AppCompatActivity {

    private OrderViewModel orderViewModel;
    private Order currentOrder;

    private TextView tvOrderId, tvStatus, tvOrderDate;
    private TextView tvReceiverName, tvReceiverPhone, tvDeliveryAddress;
    private TextView tvPaymentMethod, tvTotal;
    private RecyclerView rvOrderItems;
    private OrderItemAdapter orderItemAdapter;
    private Button btnCancelOrder, btnReturnOrder, btnReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        initViews();
        setupActions();

        String orderId = getIntent().getStringExtra("order_id");
        if (orderId != null) {
            loadOrderDetail(orderId);
        } else {
            finish();
        }
    }

    private void initViews() {
        tvOrderId         = findViewById(R.id.tv_order_id);
        tvStatus          = findViewById(R.id.tv_status);
        tvOrderDate       = findViewById(R.id.tv_order_date);
        tvReceiverName    = findViewById(R.id.tv_receiver_name);
        tvReceiverPhone   = findViewById(R.id.tv_receiver_phone);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);
        tvPaymentMethod   = findViewById(R.id.tv_payment_method);
        tvTotal           = findViewById(R.id.tv_total);
        btnCancelOrder    = findViewById(R.id.btn_cancel_order);
        btnReturnOrder    = findViewById(R.id.btn_return_order);
        btnReview         = findViewById(R.id.btn_review);
        rvOrderItems      = findViewById(R.id.rv_order_items);

        // Back button
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Order items RecyclerView
        orderItemAdapter = new OrderItemAdapter(this);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderItemAdapter);
        rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void loadOrderDetail(String orderId) {
        orderViewModel.getOrderDetail(orderId).observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                currentOrder = result.data;
                bindOrder(result.data);
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                Toast.makeText(this, "Không thể tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindOrder(Order order) {
        // Header
        String displayId = order.getOrderId() != null ? order.getOrderId() : "#" + order.getId().substring(0, 8).toUpperCase();
        tvOrderId.setText(displayId);
        tvStatus.setText(getStatusLabel(order.getStatus()));
        tvStatus.setTextColor(getResources().getColor(getStatusColor(order.getStatus()), null));

        if (order.getCreatedAt() != null && order.getCreatedAt().length() >= 10) {
            tvOrderDate.setText("Đặt lúc: " + order.getCreatedAt().replace("T", " ").substring(0, 16));
        }

        // Delivery info
        if (order.getDelivery() != null) {
            tvReceiverName.setText(order.getDelivery().getName() != null ? order.getDelivery().getName() : "—");
            tvReceiverPhone.setText(order.getDelivery().getPhone() != null ? order.getDelivery().getPhone() : "—");
            tvDeliveryAddress.setText(order.getDelivery().getAddress() != null ? order.getDelivery().getAddress() : "—");
        }

        // Payment
        if (order.getPayment() != null) {
            String method = order.getPayment().getMethod();
            tvPaymentMethod.setText(getPaymentLabel(method) + " - " + getPaymentStatusLabel(order.getPayment().getStatus()));
        } else {
            tvPaymentMethod.setText("—");
        }
        tvTotal.setText(CurrencyFormatter.format(order.getFinalAmount()));

        // Order items
        if (order.getItems() != null) {
            orderItemAdapter.setItems(order.getItems());
        }

        // Action buttons
        updateActionButtons(order);
    }

    private void updateActionButtons(Order order) {
        String status = order.getStatus() == null ? "" : order.getStatus().toLowerCase();
        btnCancelOrder.setVisibility(View.GONE);
        btnReturnOrder.setVisibility(View.GONE);
        btnReview.setVisibility(View.GONE);

        if ("pending".equals(status) || "confirmed".equals(status)) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        }
        if ("delivered".equals(status)) {
            btnReturnOrder.setVisibility(View.VISIBLE);
            btnReview.setVisibility(View.VISIBLE);
        }
    }

    private void setupActions() {
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        btnReturnOrder = findViewById(R.id.btn_return_order);
        btnReview      = findViewById(R.id.btn_review);

        btnCancelOrder.setOnClickListener(v -> showCancelDialog());
        btnReturnOrder.setOnClickListener(v -> showReturnDialog());
        btnReview.setOnClickListener(v -> showReviewDialog());
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy đơn", (d, w) -> {
                    if (currentOrder != null) {
                        orderViewModel.cancelOrder(currentOrder.getId()).observe(this, result -> {
                            if (result.status == AuthRepository.Result.Status.SUCCESS) {
                                Toast.makeText(this, "Đơn hàng đã được hủy", Toast.LENGTH_SHORT).show();
                                finish();
                            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showReturnDialog() {
        EditText etReason = new EditText(this);
        etReason.setHint("Lý do trả hàng...");
        new AlertDialog.Builder(this)
                .setTitle("Yêu cầu trả hàng")
                .setView(etReason)
                .setPositiveButton("Gửi yêu cầu", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập lý do", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (currentOrder != null) {
                        orderViewModel.returnOrder(currentOrder.getId(), reason).observe(this, result -> {
                            if (result.status == AuthRepository.Result.Status.SUCCESS) {
                                Toast.makeText(this, "Đã gửi yêu cầu trả hàng", Toast.LENGTH_SHORT).show();
                                finish();
                            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showReviewDialog() {
        if (currentOrder == null || currentOrder.getItems() == null || currentOrder.getItems().isEmpty()) {
            Toast.makeText(this, "Khong co san pham de danh gia", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.List<OrderItem> items = currentOrder.getItems();
        if (items.size() == 1) {
            openReviewSheet(items.get(0));
            return;
        }

        String[] names = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            names[i] = items.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chon san pham de danh gia")
                .setItems(names, (d, which) -> openReviewSheet(items.get(which)))
                .setNegativeButton("Dong", null)
                .show();
    }

    private void openReviewSheet(OrderItem item) {
        ReviewBottomSheetDialogFragment sheet = ReviewBottomSheetDialogFragment.newInstance(
                currentOrder.getId(),
                item.getProductId(),
                item.getName(),
                item.getImageUrl(),
                item.getPrice(),
                item.getUnit()
        );
        sheet.show(getSupportFragmentManager(), "review_sheet");
    }

    // Helpers
    private String getStatusLabel(String status) {
        if (status == null) return "—";
        switch (status.toLowerCase()) {
            case "pending":          return "Chờ xác nhận";
            case "confirmed":        return "Đã xác nhận";
            case "processing":       return "Đang xử lý";
            case "packed":           return "Đóng gói xong";
            case "shipping":         return "Đang giao";
            case "delivered":        return "Đã giao";
            case "cancelled":        return "Đã hủy";
            case "return_requested": return "Yêu cầu trả";
            case "returned":         return "Đã trả";
            case "refunded":         return "Đã hoàn tiền";
            default:                 return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return R.color.text_secondary;
        switch (status.toLowerCase()) {
            case "pending":          return R.color.status_pending;
            case "confirmed":        return R.color.status_confirmed;
            case "shipping":         return R.color.status_shipping;
            case "delivered":        return R.color.status_delivered;
            case "cancelled":        return R.color.status_cancelled;
            default:                 return R.color.status_return;
        }
    }

    private String getPaymentLabel(String method) {
        if (method == null) return "—";
        method = method.toLowerCase();
        switch (method) {
            case "cod":   return "Thanh toán khi nhận hàng (COD)";
            case "vnpay": return "VNPay";
            case "momo":  return "MoMo";
            default:      return method.toUpperCase();
        }
    }

    private String getPaymentStatusLabel(String status) {
        if (status == null) return "Pending";
        switch (status.toLowerCase()) {
            case "paid": return "Paid";
            case "failed": return "Failed";
            case "cancelled": return "Cancelled";
            default: return "Pending";
        }
    }
}
