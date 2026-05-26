package vn.vuavuive.admin.ui.shipments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.ActivityShipmentDetailBinding;
import vn.vuavuive.shared.data.dto.Shipment;
import vn.vuavuive.shared.data.dto.StatusEvent;
import vn.vuavuive.shared.data.dto.User;

public class ShipmentDetailActivity extends AppCompatActivity {

    private ActivityShipmentDetailBinding binding;
    private MockRepository repo;
    private User currentUser;
    private String shipmentId;
    private Shipment currentShipment;

    private static final String[] STATUS_NAMES = {
            "Chờ lấy hàng (Pending)",
            "Đã lấy hàng (Processing)",
            "Đang giao hàng (Shipping)",
            "Đã giao thành công (Delivered)",
            "Giao thất bại (Failed)"
    };

    private static final String[] STATUS_KEYS = {
            "pending", "processing", "shipping", "delivered", "failed"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShipmentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repo = MockRepository.getInstance();
        currentUser = repo.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        shipmentId = getIntent().getStringExtra("SHIPMENT_ID");
        if (shipmentId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin vận đơn!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupSpinner();
        loadShipmentDetails();
        setupListeners();
        enforceRolePermissions();
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, STATUS_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerShipmentStatus.setAdapter(adapter);
    }

    private void loadShipmentDetails() {
        for (Shipment s : repo.getShipments()) {
            if (s.getId().equals(shipmentId)) {
                currentShipment = s;
                break;
            }
        }

        if (currentShipment == null) {
            Toast.makeText(this, "Vận đơn không tồn tại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind Header Panel
        binding.tvDetailTracking.setText("Vận đơn: " + currentShipment.getTrackingNumber());
        
        String carrierText = "Nội bộ Vựa Vui Vẻ";
        if ("external".equalsIgnoreCase(currentShipment.getCarrier())) {
            carrierText = "Giao Hàng Nhanh (GHN)";
        }
        binding.tvDetailCarrier.setText("Nhà vận chuyển: " + carrierText);
        binding.tvDetailOrderId.setText("Mã đơn hàng gốc: " + currentShipment.getOrderId());
        
        String eta = currentShipment.getEta() != null ? currentShipment.getEta().split("T")[0] : "N/A";
        binding.tvDetailEta.setText("Dự kiến giao hàng: " + eta);

        // Bind spinner selection
        for (int i = 0; i < STATUS_KEYS.length; i++) {
            if (STATUS_KEYS[i].equalsIgnoreCase(currentShipment.getCurrentStatus())) {
                binding.spinnerShipmentStatus.setSelection(i);
                break;
            }
        }

        // Draw dynamic visual timeline
        renderTimeline();
    }

    private void renderTimeline() {
        binding.layoutTimelineContainer.removeAllViews();
        List<StatusEvent> history = currentShipment.getStatusHistory();

        if (history == null || history.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có lịch trình cập nhật");
            tvEmpty.setTextColor(0xFF888888);
            binding.layoutTimelineContainer.addView(tvEmpty);
            return;
        }

        for (int i = 0; i < history.size(); i++) {
            StatusEvent event = history.get(i);

            // Horizontal layout for each event row
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setPadding(0, 0, 0, 24);

            // LEFT SIDE: Circle indicator & vertical line
            LinearLayout markerContainer = new LinearLayout(this);
            markerContainer.setOrientation(LinearLayout.VERTICAL);
            markerContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams markerParams = new LinearLayout.LayoutParams(60, LinearLayout.LayoutParams.MATCH_PARENT);
            markerContainer.setLayoutParams(markerParams);

            // Dot element
            View dot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(24, 24);
            dotParams.setMargins(0, 8, 0, 8);
            dot.setLayoutParams(dotParams);
            
            // First item is green/teal, others are gray
            if (i == history.size() - 1) {
                dot.setBackgroundColor(0xFF00B0FF); // Active bright cyan
            } else {
                dot.setBackgroundColor(0xFF888888); // Grayed out completed
            }
            markerContainer.addView(dot);

            // Vertical line if not the last element
            if (i < history.size() - 1) {
                View line = new View(this);
                LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(4, 80);
                line.setLayoutParams(lineParams);
                line.setBackgroundColor(0xFF444444);
                markerContainer.addView(line);
            }

            row.addView(markerContainer);

            // RIGHT SIDE: Content
            LinearLayout contentContainer = new LinearLayout(this);
            contentContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            contentParams.setMargins(16, 0, 0, 0);
            contentContainer.setLayoutParams(contentParams);

            // Title Status
            TextView tvStatus = new TextView(this);
            tvStatus.setText(event.getStatus().toUpperCase());
            tvStatus.setTextColor(Color.WHITE);
            tvStatus.setTypeface(null, Typeface.BOLD);
            tvStatus.setTextSize(14);
            contentContainer.addView(tvStatus);

            // Details Note
            TextView tvNote = new TextView(this);
            tvNote.setText(event.getNote() != null ? event.getNote() : "Không có ghi chú chi tiết.");
            tvNote.setTextColor(0xFFBBBBBB);
            tvNote.setTextSize(12);
            tvNote.setPadding(0, 4, 0, 4);
            contentContainer.addView(tvNote);

            // Time String
            TextView tvTime = new TextView(this);
            String rawTime = event.getTimestamp() != null ? event.getTimestamp() : "";
            if (rawTime.contains("T")) {
                rawTime = rawTime.replace("T", " ").replace("Z", "");
            }
            tvTime.setText(rawTime);
            tvTime.setTextColor(0xFF888888);
            tvTime.setTextSize(10);
            contentContainer.addView(tvTime);

            row.addView(contentContainer);

            // Add the row to timeline container
            binding.layoutTimelineContainer.addView(row);
        }
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(int sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveShipment.setOnClickListener(v -> saveShipmentStatus());
    }

    private void enforceRolePermissions() {
        if ("audit".equals(currentUser.getRole())) {
            // Disable spinner & note
            binding.spinnerShipmentStatus.setEnabled(false);
            binding.etShipmentNote.setEnabled(false);

            // Hide or lock save button
            binding.btnSaveShipment.setText("CHẾ ĐỘ XEM TIN (READ ONLY)");
            binding.btnSaveShipment.setBackgroundColor(0xFF555555);
            binding.btnSaveShipment.setOnClickListener(v -> {
                Toast.makeText(this, "Kiểm toán viên không có quyền cập nhật trạng thái vận đơn", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveShipmentStatus() {
        int pos = binding.spinnerShipmentStatus.getSelectedItemPosition();
        String selectedStatus = STATUS_KEYS[pos];
        String note = binding.etShipmentNote.getText().toString().trim();

        if (note.isEmpty()) {
            binding.etShipmentNote.setError("Vui lòng ghi chú lý do/tình trạng giao nhận");
            return;
        }

        repo.updateShipmentStatus(currentShipment.getId(), selectedStatus, note);
        Toast.makeText(this, "Cập nhật trạng thái vận đơn thành công!", Toast.LENGTH_SHORT).show();
        
        // Reset and reload
        binding.etShipmentNote.setText("");
        loadShipmentDetails();
    }
}
