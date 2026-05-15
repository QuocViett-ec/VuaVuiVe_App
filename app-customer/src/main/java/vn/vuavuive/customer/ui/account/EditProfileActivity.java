package vn.vuavuive.customer.ui.account;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.shared.data.dto.User;

@AndroidEntryPoint
public class EditProfileActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    private TextInputLayout tilName, tilPhone, tilAddress;
    private TextInputEditText etName, etPhone, etAddress;
    private MaterialButton btnSave;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        prefillCurrentData();
        setupSave();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tilName    = findViewById(R.id.til_name);
        tilPhone   = findViewById(R.id.til_phone);
        tilAddress = findViewById(R.id.til_address);
        etName     = findViewById(R.id.et_name);
        etPhone    = findViewById(R.id.et_phone);
        etAddress  = findViewById(R.id.et_address);
        btnSave    = findViewById(R.id.btn_save);
        progressBar = findViewById(R.id.progress_bar);
        tvError    = findViewById(R.id.tv_error);
    }

    private void prefillCurrentData() {
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                etName.setText(user.getName());
                etPhone.setText(user.getPhone());
                etAddress.setText(user.getAddress());
            }
        });
    }

    private void setupSave() {
        btnSave.setOnClickListener(v -> {
            String name    = getText(etName);
            String phone   = getText(etPhone);
            String address = getText(etAddress);

            if (name.length() < 2) {
                tilName.setError("Họ tên phải có ít nhất 2 ký tự");
                return;
            }
            tilName.setError(null);
            setLoading(true);

            authViewModel.updateProfile(name, phone, address).observe(this, result -> {
                setLoading(false);
                if (result.status == AuthRepository.Result.Status.SUCCESS) {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (result.status == AuthRepository.Result.Status.ERROR) {
                    tvError.setText(result.message);
                    tvError.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        btnSave.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
