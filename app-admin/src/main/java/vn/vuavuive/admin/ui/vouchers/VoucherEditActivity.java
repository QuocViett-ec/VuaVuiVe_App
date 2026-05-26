package vn.vuavuive.admin.ui.vouchers;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.ActivityVoucherEditBinding;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.Voucher;

public class VoucherEditActivity extends AppCompatActivity {

    private ActivityVoucherEditBinding binding;
    private MockRepository repo;
    private User currentUser;
    private String voucherCode;
    private Voucher existingVoucher;

    private static final String[] VOUCHER_TYPE_NAMES = {
            "Miễn phí vận chuyển (ship)",
            "Giảm theo phần trăm (percent)",
            "Giảm tiền mặt cố định (fixed)"
    };

    private static final String[] VOUCHER_TYPE_KEYS = {
            "ship", "percent", "fixed"
    };

    private final Calendar startsCalendar = Calendar.getInstance();
    private final Calendar expiresCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVoucherEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repo = MockRepository.getInstance();
        currentUser = repo.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        voucherCode = getIntent().getStringExtra("VOUCHER_CODE");
        setupSpinner();
        setupDatePickers();
        setupListeners();

        if (voucherCode != null) {
            loadExistingVoucher();
        } else {
            binding.tvTitle.setText("THÊM KHUYẾN MÃI MỚI");
            // Set default date values
            binding.etVoucherStarts.setText(dateFormat.format(new Date()));
            expiresCalendar.add(Calendar.MONTH, 1);
            binding.etVoucherExpires.setText(dateFormat.format(expiresCalendar.getTime()));
        }

        enforceRolePermissions();
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, VOUCHER_TYPE_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerVoucherType.setAdapter(adapter);

        binding.spinnerVoucherType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = VOUCHER_TYPE_KEYS[position];
                if ("percent".equals(type)) {
                    binding.layoutCap.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutCap.setVisibility(View.GONE);
                    binding.etVoucherCap.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDatePickers() {
        DatePickerDialog.OnDateSetListener startDateSet = (view, year, month, dayOfMonth) -> {
            startsCalendar.set(Calendar.YEAR, year);
            startsCalendar.set(Calendar.MONTH, month);
            startsCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.etVoucherStarts.setText(dateFormat.format(startsCalendar.getTime()));
        };

        DatePickerDialog.OnDateSetListener expireDateSet = (view, year, month, dayOfMonth) -> {
            expiresCalendar.set(Calendar.YEAR, year);
            expiresCalendar.set(Calendar.MONTH, month);
            expiresCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.etVoucherExpires.setText(dateFormat.format(expiresCalendar.getTime()));
        };

        binding.etVoucherStarts.setOnClickListener(v -> {
            if (!"admin".equals(currentUser.getRole())) return;
            new DatePickerDialog(this, startDateSet,
                    startsCalendar.get(Calendar.YEAR),
                    startsCalendar.get(Calendar.MONTH),
                    startsCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        binding.etVoucherExpires.setOnClickListener(v -> {
            if (!"admin".equals(currentUser.getRole())) return;
            new DatePickerDialog(this, expireDateSet,
                    expiresCalendar.get(Calendar.YEAR),
                    expiresCalendar.get(Calendar.MONTH),
                    expiresCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void loadExistingVoucher() {
        binding.tvTitle.setText("CHỈNH SỬA KHUYẾN MÃI");
        for (Voucher v : repo.getVouchers()) {
            if (v.getCode().equalsIgnoreCase(voucherCode)) {
                existingVoucher = v;
                break;
            }
        }

        if (existingVoucher != null) {
            // Can't edit the code of an existing voucher (primary key)
            binding.etVoucherCode.setText(existingVoucher.getCode());
            binding.etVoucherCode.setEnabled(false);

            binding.etVoucherValue.setText(String.format(Locale.US, "%.0f", existingVoucher.getValue()));
            binding.etVoucherCap.setText(String.format(Locale.US, "%.0f", existingVoucher.getCap()));
            binding.etVoucherMinOrder.setText(String.format(Locale.US, "%.0f", existingVoucher.getMinOrderValue()));
            binding.etVoucherMaxUses.setText(String.valueOf(existingVoucher.getMaxUses()));
            binding.etVoucherNote.setText(existingVoucher.getNote());
            binding.switchVoucherActive.setChecked(existingVoucher.isActive());

            // Bind start and end dates
            if (existingVoucher.getStartsAt() != null) {
                String cleanStart = existingVoucher.getStartsAt().split("T")[0];
                binding.etVoucherStarts.setText(cleanStart);
                try {
                    startsCalendar.setTime(dateFormat.parse(cleanStart));
                } catch (Exception ignored) {}
            }
            if (existingVoucher.getExpiresAt() != null) {
                String cleanExpire = existingVoucher.getExpiresAt().split("T")[0];
                binding.etVoucherExpires.setText(cleanExpire);
                try {
                    expiresCalendar.setTime(dateFormat.parse(cleanExpire));
                } catch (Exception ignored) {}
            }

            // Set spinner selection
            for (int i = 0; i < VOUCHER_TYPE_KEYS.length; i++) {
                if (VOUCHER_TYPE_KEYS[i].equalsIgnoreCase(existingVoucher.getType())) {
                    binding.spinnerVoucherType.setSelection(i);
                    break;
                }
            }
        } else {
            Toast.makeText(this, "Không tìm thấy khuyến mãi!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveVoucher.setOnClickListener(v -> saveVoucher());
    }

    private void enforceRolePermissions() {
        if (!"admin".equals(currentUser.getRole())) {
            // Read-only access: disable inputs
            binding.etVoucherCode.setEnabled(false);
            binding.spinnerVoucherType.setEnabled(false);
            binding.etVoucherValue.setEnabled(false);
            binding.etVoucherCap.setEnabled(false);
            binding.etVoucherMinOrder.setEnabled(false);
            binding.etVoucherMaxUses.setEnabled(false);
            binding.etVoucherStarts.setEnabled(false);
            binding.etVoucherExpires.setEnabled(false);
            binding.etVoucherNote.setEnabled(false);
            binding.switchVoucherActive.setEnabled(false);

            // Re-style save button as read-only message
            binding.btnSaveVoucher.setText("CHẾ ĐỘ XEM TIN (READ ONLY)");
            binding.btnSaveVoucher.setBackgroundColor(0xFF555555);
            binding.btnSaveVoucher.setOnClickListener(v -> {
                Toast.makeText(this, "Bạn không có quyền quản trị để chỉnh sửa khuyến mãi", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveVoucher() {
        String code = binding.etVoucherCode.getText().toString().trim().toUpperCase();
        String valueStr = binding.etVoucherValue.getText().toString().trim();
        String capStr = binding.etVoucherCap.getText().toString().trim();
        String minOrderStr = binding.etVoucherMinOrder.getText().toString().trim();
        String maxUsesStr = binding.etVoucherMaxUses.getText().toString().trim();
        String startsStr = binding.etVoucherStarts.getText().toString().trim();
        String expiresStr = binding.etVoucherExpires.getText().toString().trim();
        String note = binding.etVoucherNote.getText().toString().trim();
        boolean isActive = binding.switchVoucherActive.isChecked();

        // 1. Validation
        if (code.isEmpty()) {
            binding.etVoucherCode.setError("Vui lòng nhập mã khuyến mãi");
            return;
        }

        // Duplicate code verification on create mode
        if (existingVoucher == null) {
            for (Voucher v : repo.getVouchers()) {
                if (v.getCode().equalsIgnoreCase(code)) {
                    binding.etVoucherCode.setError("Mã khuyến mãi này đã tồn tại");
                    return;
                }
            }
        }

        double value;
        try {
            value = Double.parseDouble(valueStr);
            if (value <= 0) {
                binding.etVoucherValue.setError("Giá trị giảm phải lớn hơn 0");
                return;
            }
        } catch (NumberFormatException e) {
            binding.etVoucherValue.setError("Giá trị không hợp lệ");
            return;
        }

        int typePos = binding.spinnerVoucherType.getSelectedItemPosition();
        String type = VOUCHER_TYPE_KEYS[typePos];

        if ("percent".equals(type) && value > 100) {
            binding.etVoucherValue.setError("Mức giảm phần trăm không được vượt quá 100%");
            return;
        }

        double cap = 0;
        if ("percent".equals(type)) {
            try {
                cap = Double.parseDouble(capStr);
                if (cap < 0) {
                    binding.etVoucherCap.setError("Giảm tối đa không được âm");
                    return;
                }
            } catch (NumberFormatException e) {
                binding.etVoucherCap.setError("Giảm tối đa không hợp lệ");
                return;
            }
        }

        double minOrder;
        try {
            minOrder = Double.parseDouble(minOrderStr);
            if (minOrder < 0) {
                binding.etVoucherMinOrder.setError("Đơn tối thiểu không được âm");
                return;
            }
        } catch (NumberFormatException e) {
            binding.etVoucherMinOrder.setError("Đơn tối thiểu không hợp lệ");
            return;
        }

        int maxUses;
        try {
            maxUses = Integer.parseInt(maxUsesStr);
            if (maxUses <= 0) {
                binding.etVoucherMaxUses.setError("Lượt dùng phải lớn hơn 0");
                return;
            }
        } catch (NumberFormatException e) {
            binding.etVoucherMaxUses.setError("Lượt dùng không hợp lệ");
            return;
        }

        if (startsStr.isEmpty()) {
            binding.etVoucherStarts.setError("Vui lòng nhập ngày bắt đầu");
            return;
        }

        if (expiresStr.isEmpty()) {
            binding.etVoucherExpires.setError("Vui lòng nhập ngày kết thúc");
            return;
        }

        // Compare dates
        try {
            Date start = dateFormat.parse(startsStr);
            Date expire = dateFormat.parse(expiresStr);
            if (expire.before(start)) {
                binding.etVoucherExpires.setError("Ngày hết hạn không được trước ngày bắt đầu");
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ngày tháng không đúng định dạng yyyy-MM-dd", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Save
        Voucher v = existingVoucher != null ? existingVoucher : new Voucher();
        v.setCode(code);
        v.setType(type);
        v.setValue(value);
        v.setCap(cap);
        v.setMinOrderValue(minOrder);
        v.setMaxUses(maxUses);
        v.setStartsAt(startsStr + "T00:00:00Z");
        v.setExpiresAt(expiresStr + "T23:59:59Z");
        v.setNote(note.isEmpty() ? ("Giảm khuyến mãi " + code) : note);
        v.setActive(isActive);

        if (existingVoucher != null) {
            repo.updateVoucher(voucherCode, v);
            Toast.makeText(this, "Cập nhật mã khuyến mãi thành công", Toast.LENGTH_SHORT).show();
        } else {
            v.setActive(true);
            repo.addVoucher(v);
            Toast.makeText(this, "Đã tạo mã khuyến mãi thành công", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
