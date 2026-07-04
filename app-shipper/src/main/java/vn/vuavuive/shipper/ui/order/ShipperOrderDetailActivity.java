package vn.vuavuive.shipper.ui.order;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import dagger.hilt.android.AndroidEntryPoint;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shipper.data.repository.FirebaseShipperRepository;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.OrderItem;
import vn.vuavuive.shared.data.dto.PaymentDetail;

/**
 * ShipperOrderDetailActivity — Chi tiết đơn hàng dành cho Shipper (Firebase-based).
 *
 * Tính năng:
 * - Hiển thị đầy đủ thông tin khách hàng, địa chỉ, danh sách sản phẩm
 * - Nút Quick Call: Gọi điện trực tiếp (Intent ACTION_DIAL)
 * - Nút Navigate: Mở Google Maps chỉ đường
 * - Cập nhật trạng thái đơn hàng qua Firebase RTDB:
 *     CONFIRMED / PREPARING / READY_FOR_PICKUP → "Bắt đầu giao hàng" → IN_TRANSIT
 *     IN_TRANSIT / SHIPPING → "Đã giao thành công" → DELIVERED
 *               → "Giao thất bại"       → FAILED
 *     DELIVERED / FAILED / RETURNED → Hiển thị label kết thúc
 */
@AndroidEntryPoint
public class ShipperOrderDetailActivity extends AppCompatActivity {

    @Inject FirebaseShipperRepository repository;

    private String orderId;

    // Views
    private TextView tvOrderId, tvOrderDate, tvHeaderStatus;
    private TextView tvCustomerName, tvPhone, tvAddress, tvNote;
    private TextView tvTotal, tvPaymentMethod, tvDoneLabel;
    private MaterialButton btnCall, btnNavigate;
    private MaterialButton btnStartDelivery, btnDelivered, btnFailed;
    private View layoutNote;
    private RecyclerView recyclerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_order_detail);

        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) { finish(); return; }

        initViews();
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAndBind();
    }

    private void initViews() {
        tvOrderId        = findViewById(R.id.tv_order_id);
        tvOrderDate      = findViewById(R.id.tv_order_date);
        tvHeaderStatus   = findViewById(R.id.tv_header_status);
        tvCustomerName   = findViewById(R.id.tv_customer_name);
        tvPhone          = findViewById(R.id.tv_phone);
        tvAddress        = findViewById(R.id.tv_address);
        tvNote           = findViewById(R.id.tv_note);
        tvTotal          = findViewById(R.id.tv_total);
        tvPaymentMethod  = findViewById(R.id.tv_payment_method);
        tvDoneLabel      = findViewById(R.id.tv_done_label);
        layoutNote       = findViewById(R.id.layout_note);
        btnCall          = findViewById(R.id.btn_call);
        btnNavigate      = findViewById(R.id.btn_navigate);
        btnStartDelivery = findViewById(R.id.btn_start_delivery);
        btnDelivered     = findViewById(R.id.btn_delivered);
        btnFailed        = findViewById(R.id.btn_failed);
        recyclerItems    = findViewById(R.id.recycler_items);
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerItems.setNestedScrollingEnabled(false);
    }

    /** Lấy chi tiết đơn hàng từ Firebase RTDB (one-shot). */
    private void fetchAndBind() {
        repository.getOrderDetail(orderId).observe(this, result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    // Có thể hiện spinner nhỏ nếu cần
                    break;
                case SUCCESS:
                    if (result.data != null) bindOrder(result.data);
                    break;
                case ERROR:
                    Toast.makeText(this,
                            result.message != null ? result.message : "Không tải được thông tin đơn hàng",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void bindOrder(Order order) {
        // ── Header ──────────────────────────────────────────────────
        String idShort = (order.getId() != null && order.getId().length() >= 8)
                ? "#" + order.getId().substring(0, 8).toUpperCase() : "#------";
        tvOrderId.setText(idShort);
        tvOrderDate.setText("🗓 " + formatDate(order.getCreatedAt()));
        bindStatusBadge(order.getStatus());

        // ── Delivery info ────────────────────────────────────────────
        String phone   = order.getRecipientPhone();
        String address = order.getRecipientAddress();
        String note    = order.getNote();

        tvCustomerName.setText(order.getRecipientName() != null
                ? order.getRecipientName() : "Khách hàng");
        tvPhone.setText(phone != null ? phone : "—");
        tvAddress.setText(address != null ? address : "Chưa có địa chỉ");

        if (note != null && !note.isEmpty()) {
            tvNote.setText(note);
            layoutNote.setVisibility(View.VISIBLE);
        } else {
            layoutNote.setVisibility(View.GONE);
        }

        // ── Quick Call ───────────────────────────────────────────────
        if (phone != null && !phone.isEmpty()) {
            String finalPhone = phone.trim();
            btnCall.setEnabled(true);
            btnCall.setOnClickListener(v ->
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + finalPhone))));
        } else {
            btnCall.setEnabled(false);
        }

        // ── Navigate (Google Maps) ───────────────────────────────────
        if (address != null && !address.isEmpty()) {
            String finalAddress = address.trim();
            btnNavigate.setEnabled(true);
            btnNavigate.setOnClickListener(v -> {
                Uri mapsUri = Uri.parse("google.navigation:q=" + Uri.encode(finalAddress));
                Intent mapsIntent = new Intent(Intent.ACTION_VIEW, mapsUri);
                mapsIntent.setPackage("com.google.android.apps.maps");
                if (mapsIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapsIntent);
                } else {
                    Uri fallback = Uri.parse("https://www.google.com/maps/search/?api=1&query="
                            + Uri.encode(finalAddress));
                    startActivity(new Intent(Intent.ACTION_VIEW, fallback));
                }
            });
        } else {
            btnNavigate.setEnabled(false);
        }

        // ── Total & Payment ──────────────────────────────────────────
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvTotal.setText(fmt.format((long) order.getFinalAmount()) + " đ");
        PaymentDetail pmt = order.getPayment();
        tvPaymentMethod.setText(paymentText(pmt, order.getFinalAmount()));

        // ── Items ────────────────────────────────────────────────────
        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            recyclerItems.setAdapter(new ShipperOrderItemAdapter(this, items));
        }

        // ── Action Buttons ───────────────────────────────────────────
        setupActionButtons(order);
    }

    private void setupActionButtons(Order order) {
        String status = order.getStatus();
        status = status == null ? "" : status.toUpperCase();
        btnStartDelivery.setVisibility(View.GONE);
        btnDelivered.setVisibility(View.GONE);
        btnFailed.setVisibility(View.GONE);
        tvDoneLabel.setVisibility(View.GONE);

        if ("CONFIRMED".equals(status) || "PREPARING".equals(status)
                || "READY_FOR_PICKUP".equals(status)) {
            btnStartDelivery.setVisibility(View.VISIBLE);
            btnStartDelivery.setOnClickListener(v ->
                    confirm("Bắt đầu giao hàng?",
                            "Xác nhận bạn đã lấy hàng và bắt đầu giao cho khách?",
                            "IN_TRANSIT"));

        } else if ("IN_TRANSIT".equals(status) || "SHIPPING".equals(status)) {
            btnDelivered.setVisibility(View.VISIBLE);
            btnFailed.setVisibility(View.VISIBLE);
            btnDelivered.setOnClickListener(v ->
                    confirm("Xác nhận giao thành công?",
                            "Đơn hàng sẽ được đánh dấu là đã giao thành công.",
                            "DELIVERED"));
            btnFailed.setOnClickListener(v ->
                    showFailReasonDialog());
        } else {
            // Terminal state
            String doneText = "DELIVERED".equals(status) ? "✅ Đơn hàng đã giao thành công"
                    : "FAILED".equals(status) ? "❌ Đơn hàng giao thất bại" + 
                      (order.getFailReason() != null && !order.getFailReason().isEmpty() ? "\nLý do: " + order.getFailReason() : "")
                    : "Đơn hàng đã kết thúc";
            tvDoneLabel.setText(doneText);
            tvDoneLabel.setVisibility(View.VISIBLE);
        }
    }

    private void showFailReasonDialog() {
        String[] reasons = {
            "Khách hàng không nghe máy (Gọi nhiều lần)",
            "Sai địa chỉ giao hàng / Không tìm thấy",
            "Khách từ chối nhận (Thay đổi ý định / Không có tiền)",
            "Khách hẹn giao lại vào thời gian khác",
            "Lý do khác (Nhập tay)"
        };

        final int[] selectedIndex = {0};

        new AlertDialog.Builder(this)
                .setTitle("Lý do giao hàng thất bại")
                .setSingleChoiceItems(reasons, 0, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String selectedReason = reasons[selectedIndex[0]];
                    if ("Lý do khác (Nhập tay)".equals(selectedReason)) {
                        showCustomFailReasonDialog();
                    } else {
                        updateStatus("FAILED", selectedReason);
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showCustomFailReasonDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập lý do chi tiết...");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        container.addView(input);
        input.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        container.setPadding(padding, padding / 2, padding, padding / 2);

        new AlertDialog.Builder(this)
                .setTitle("Lý do giao hàng thất bại khác")
                .setView(container)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        reason = "Lý do khác";
                    }
                    updateStatus("FAILED", reason);
                })
                .setNegativeButton("Quay lại", (dialog, which) -> showFailReasonDialog())
                .show();
    }

    private void confirm(String title, String message, String newStatus) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xác nhận", (d, w) -> updateStatus(newStatus))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    /** Cập nhật status trực tiếp vào Firebase RTDB. */
    private void updateStatus(String newStatus) {
        updateStatus(newStatus, null);
    }

    private void updateStatus(String newStatus, String failReason) {
        LiveData<FirebaseShipperRepository.Result<Void>> liveData =
                repository.updateOrderStatus(orderId, newStatus, failReason);

        liveData.observe(this, result -> {
            if (result == null) return;
            if (result.status == FirebaseShipperRepository.Result.Status.LOADING) return;

            liveData.removeObservers(this); // one-shot

            if (result.status == FirebaseShipperRepository.Result.Status.SUCCESS) {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                fetchAndBind(); // Reload để refresh UI
            } else {
                Toast.makeText(this,
                        result.message != null ? result.message : "Cập nhật thất bại",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void bindStatusBadge(String status) {
        if (status == null) return;
        switch (status.toUpperCase()) {
            case "CONFIRMED":
            case "PREPARING":
            case "READY_FOR_PICKUP":
                tvHeaderStatus.setText("CHỜ LẤY HÀNG");
                tvHeaderStatus.setBackgroundColor(Color.parseColor("#FF9800")); break;
            case "SHIPPING":
            case "IN_TRANSIT":
                tvHeaderStatus.setText("ĐANG GIAO");
                tvHeaderStatus.setBackgroundColor(Color.parseColor("#FF6B35")); break;
            case "DELIVERED":
                tvHeaderStatus.setText("ĐÃ GIAO");
                tvHeaderStatus.setBackgroundColor(Color.parseColor("#1B8A3A")); break;
            case "FAILED":
                tvHeaderStatus.setText("THẤT BẠI");
                tvHeaderStatus.setBackgroundColor(Color.parseColor("#757575")); break;
            default:
                tvHeaderStatus.setText(status);
                tvHeaderStatus.setBackgroundColor(Color.parseColor("#9E9E9E"));
        }
    }

    private String paymentText(PaymentDetail pmt, double total) {
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        if (pmt != null && "momo".equalsIgnoreCase(pmt.getMethod())) {
            if ("paid".equalsIgnoreCase(pmt.getStatus())) return "Payment: Paid by MoMo. Do not collect cash.";
            return "Payment: MoMo " + (pmt.getStatus() == null ? "pending" : pmt.getStatus())
                    + ". Online payment has not been completed.";
        }
        return "Payment: COD. Amount to collect: " + fmt.format((long) total) + " đ";
    }

    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        try {
            String[] parts = raw.split("T");
            String[] d = parts[0].split("-");
            String t  = parts.length > 1 ? parts[1].substring(0, 5) : "";
            return d[2] + "/" + d[1] + "/" + d[0] + " · " + t;
        } catch (Exception e) { return raw; }
    }
}
