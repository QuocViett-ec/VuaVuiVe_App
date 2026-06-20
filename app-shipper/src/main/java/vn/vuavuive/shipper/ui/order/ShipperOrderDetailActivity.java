package vn.vuavuive.shipper.ui.order;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.api.ShipperOrderApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.OrderItem;
import vn.vuavuive.shared.data.dto.PaymentDetail;
import vn.vuavuive.shared.data.dto.ShipperProfile;
import vn.vuavuive.shared.util.SessionManager;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;

/**
 * ShipperOrderDetailActivity — Chi tiết đơn hàng dành cho Shipper.
 *
 * Tính năng:
 * - Hiển thị đầy đủ thông tin khách hàng, địa chỉ, danh sách sản phẩm
 * - Nút Quick Call: Gọi điện trực tiếp cho khách hàng (Intent ACTION_DIAL)
 * - Nút Navigate:  Mở Google Maps chỉ đường (google.navigation:q=...)
 * - Nút cập nhật trạng thái (động theo trạng thái hiện tại của đơn):
 *     PREPARING / READY_FOR_PICKUP → "Bắt đầu giao hàng" → IN_TRANSIT
 *     IN_TRANSIT → "Đã giao thành công" → DELIVERED
 *                → "Giao thất bại"      → FAILED
 *     DELIVERED / FAILED / RETURNED → Label hoàn thành, ẩn các nút action
 */
@AndroidEntryPoint
public class ShipperOrderDetailActivity extends AppCompatActivity {

    @Inject ShipperOrderApi shipperOrderApi;
    @Inject OrderApi orderApi;
    @Inject SessionManager sessionManager;

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

    private void fetchAndBind() {
        orderApi.getOrderDetail(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    bindOrder(response.body().getData());
                } else {
                    Toast.makeText(ShipperOrderDetailActivity.this,
                            "Không tải được thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Toast.makeText(ShipperOrderDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
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
        }

        // ── Quick Call ───────────────────────────────────────────────
        if (phone != null && !phone.isEmpty()) {
            String finalPhone = phone.trim();
            btnCall.setOnClickListener(v ->
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + finalPhone))));
        }

        // ── Navigate (Google Maps) ───────────────────────────────────
        if (address != null && !address.isEmpty()) {
            String finalAddress = address.trim();
            btnNavigate.setOnClickListener(v -> {
                Uri mapsUri = Uri.parse("google.navigation:q=" + Uri.encode(finalAddress));
                Intent mapsIntent = new Intent(Intent.ACTION_VIEW, mapsUri);
                mapsIntent.setPackage("com.google.android.apps.maps");
                if (mapsIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapsIntent);
                } else {
                    // Fallback: open in browser
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
        setupActionButtons(order.getStatus());
    }

    private void setupActionButtons(String status) {
        status = status == null ? "" : status.toUpperCase();
        btnStartDelivery.setVisibility(View.GONE);
        btnDelivered.setVisibility(View.GONE);
        btnFailed.setVisibility(View.GONE);
        tvDoneLabel.setVisibility(View.GONE);

        if ("CONFIRMED".equals(status) || "PREPARING".equals(status) || "READY_FOR_PICKUP".equals(status) || "SHIPPING".equals(status)) {
            btnStartDelivery.setVisibility(View.VISIBLE);
            btnStartDelivery.setOnClickListener(v ->
                    confirm("Bắt đầu giao hàng?",
                            "Xác nhận bạn đã lấy hàng và bắt đầu giao cho khách?",
                            "IN_TRANSIT"));

        } else if ("IN_TRANSIT".equals(status)) {
            btnDelivered.setVisibility(View.VISIBLE);
            btnFailed.setVisibility(View.VISIBLE);
            btnDelivered.setOnClickListener(v ->
                    confirm("Xác nhận giao thành công?",
                            "Đơn hàng sẽ được đánh dấu là đã giao thành công.",
                            "DELIVERED"));
            btnFailed.setOnClickListener(v ->
                    confirm("Xác nhận giao thất bại?",
                            "Đơn hàng sẽ được đánh dấu là giao thất bại.",
                            "FAILED"));

        } else {
            // Terminal state
            String doneText = "DELIVERED".equals(status)
                    ? "✅ Đơn hàng đã giao thành công"
                    : "FAILED".equals(status) ? "❌ Đơn hàng giao thất bại"
                    : "Đơn hàng đã kết thúc";
            tvDoneLabel.setText(doneText);
            tvDoneLabel.setVisibility(View.VISIBLE);
        }
    }

    private void confirm(String title, String message, String newStatus) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xác nhận", (d, w) -> updateStatus(newStatus))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void updateStatus(String newStatus) {
        shipperOrderApi.getMyProfile().enqueue(new Callback<ApiResponse<ShipperProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<ShipperProfile>> call, Response<ApiResponse<ShipperProfile>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData().getId() == null) {
                    Toast.makeText(ShipperOrderDetailActivity.this, "Khong tai duoc thong tin shipper", Toast.LENGTH_SHORT).show();
                    return;
                }
                shipperOrderApi.updateDeliveryStatus(response.body().getData().getId(), orderId, newStatus, "")
                        .enqueue(new Callback<ApiResponse<Void>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                                String msg = response.isSuccessful() ? "Cap nhat thanh cong" : "Cap nhat that bai";
                                Toast.makeText(ShipperOrderDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                                if (response.isSuccessful()) fetchAndBind();
                            }
                            @Override
                            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                                Toast.makeText(ShipperOrderDetailActivity.this, "Loi ket noi", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<ApiResponse<ShipperProfile>> call, Throwable t) {
                Toast.makeText(ShipperOrderDetailActivity.this, "Loi ket noi", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Helpers ─────────────────────────────────────────────────────────────

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
