package vn.vuavuive.customer.ui.order;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.review.ReviewBottomSheetDialogFragment;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
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
        tvOrderId = findViewById(R.id.tv_order_id);
        tvStatus = findViewById(R.id.tv_status);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvReceiverName = findViewById(R.id.tv_receiver_name);
        tvReceiverPhone = findViewById(R.id.tv_receiver_phone);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvTotal = findViewById(R.id.tv_total);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        btnReturnOrder = findViewById(R.id.btn_return_order);
        btnReview = findViewById(R.id.btn_review);
        rvOrderItems = findViewById(R.id.rv_order_items);

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

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
                Toast.makeText(this, "Khong the tai chi tiet don hang", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindOrder(Order order) {
        String displayId = order.getOrderId() != null
                ? order.getOrderId()
                : "#" + order.getId().substring(0, 8).toUpperCase();
        tvOrderId.setText(displayId);
        tvStatus.setText(getStatusLabel(order.getStatus()));
        tvStatus.setTextColor(getResources().getColor(getStatusColor(order.getStatus()), null));

        if (order.getCreatedAt() != null && order.getCreatedAt().length() >= 10) {
            tvOrderDate.setText("Dat luc: " + order.getCreatedAt().replace("T", " ").substring(0, 16));
        }

        tvReceiverName.setText(order.getRecipientName() != null ? order.getRecipientName() : "-");
        tvReceiverPhone.setText(order.getRecipientPhone() != null ? order.getRecipientPhone() : "-");
        tvDeliveryAddress.setText(order.getRecipientAddress() != null ? order.getRecipientAddress() : "-");

        if (order.getPayment() != null) {
            String method = order.getPayment().getMethod();
            tvPaymentMethod.setText(getPaymentLabel(method) + " - " + getPaymentStatusLabel(order.getPayment().getStatus()));
        } else {
            tvPaymentMethod.setText("-");
        }
        tvTotal.setText(CurrencyFormatter.format(order.getFinalAmount()));

        if (order.getItems() != null) {
            orderItemAdapter.setItems(order.getItems());
        }

        updateActionButtons(order);
    }

    private void updateActionButtons(Order order) {
        String status = order.getStatus() == null ? "" : order.getStatus().toLowerCase();
        btnCancelOrder.setVisibility(View.GONE);
        btnReturnOrder.setVisibility(View.GONE);
        btnReview.setVisibility(View.GONE);

        if ("pending".equals(status)
                || "pending_payment".equals(status)
                || "pending_approval".equals(status)
                || "confirmed".equals(status)) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        }
        if ("delivered".equals(status)) {
            btnReturnOrder.setVisibility(View.VISIBLE);
            btnReview.setVisibility(View.VISIBLE);
        }
    }

    private void setupActions() {
        btnCancelOrder.setOnClickListener(v -> showCancelDialog());
        btnReturnOrder.setOnClickListener(v -> showReturnDialog());
        btnReview.setOnClickListener(v -> showReviewDialog());
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Huy don hang")
                .setMessage("Ban co chac muon huy don hang nay khong?")
                .setPositiveButton("Huy don", (d, w) -> {
                    if (currentOrder != null) {
                        orderViewModel.cancelOrder(currentOrder.getId()).observe(this, result -> {
                            if (result.status == AuthRepository.Result.Status.SUCCESS) {
                                Toast.makeText(this, "Don hang da duoc huy", Toast.LENGTH_SHORT).show();
                                finish();
                            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Dong", null)
                .show();
    }

    private void showReturnDialog() {
        EditText etReason = new EditText(this);
        etReason.setHint("Ly do tra hang...");
        new AlertDialog.Builder(this)
                .setTitle("Yeu cau tra hang")
                .setView(etReason)
                .setPositiveButton("Gui yeu cau", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Vui long nhap ly do", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (currentOrder != null) {
                        orderViewModel.returnOrder(currentOrder.getId(), reason).observe(this, result -> {
                            if (result.status == AuthRepository.Result.Status.SUCCESS) {
                                Toast.makeText(this, "Da gui yeu cau tra hang", Toast.LENGTH_SHORT).show();
                                finish();
                            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Dong", null)
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

    private String getStatusLabel(String status) {
        if (status == null) return "-";
        switch (status.toLowerCase()) {
            case "pending":
                return "Cho xac nhan";
            case "pending_payment":
                return "Cho thanh toan";
            case "pending_approval":
                return "Cho admin duyet";
            case "confirmed":
                return "Da xac nhan";
            case "processing":
                return "Dang xu ly";
            case "packed":
                return "Dong goi xong";
            case "shipping":
            case "in_transit":
                return "Dang giao";
            case "delivered":
                return "Da giao";
            case "cancelled":
                return "Da huy";
            case "return_requested":
                return "Yeu cau tra";
            case "returned":
                return "Da tra";
            case "refunded":
                return "Da hoan tien";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return R.color.text_secondary;
        switch (status.toLowerCase()) {
            case "pending":
            case "pending_payment":
            case "pending_approval":
                return R.color.status_pending;
            case "confirmed":
                return R.color.status_confirmed;
            case "shipping":
            case "in_transit":
                return R.color.status_shipping;
            case "delivered":
                return R.color.status_delivered;
            case "cancelled":
                return R.color.status_cancelled;
            default:
                return R.color.status_return;
        }
    }

    private String getPaymentLabel(String method) {
        if (method == null) return "-";
        method = method.toLowerCase();
        switch (method) {
            case "cod":
                return "Thanh toan khi nhan hang (COD)";
            case "momo":
                return "MoMo";
            case "zalopay":
                return "ZaloPay";
            default:
                return method.toUpperCase();
        }
    }

    private String getPaymentStatusLabel(String status) {
        if (status == null) return "Pending";
        switch (status.toLowerCase()) {
            case "paid":
                return "Paid";
            case "failed":
                return "Failed";
            case "cancelled":
                return "Cancelled";
            default:
                return "Pending";
        }
    }
}
