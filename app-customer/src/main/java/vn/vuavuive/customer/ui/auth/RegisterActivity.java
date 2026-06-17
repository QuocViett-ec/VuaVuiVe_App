package vn.vuavuive.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.ui.MainActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.shared.data.dto.request.RegisterRequest;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    private TextInputLayout tilName, tilPhone, tilEmail, tilPassword, tilConfirmPassword, tilAddress;
    private TextInputEditText etName, etPhone, etEmail, etPassword, etConfirmPassword, etAddress;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvError, tvLoginLink;

    // Vietnamese phone pattern: 0[3-9]xxxxxxxx
    private static final java.util.regex.Pattern PHONE_PATTERN =
            java.util.regex.Pattern.compile("^0[3-9][0-9]{8}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilName            = findViewById(R.id.til_name);
        tilPhone           = findViewById(R.id.til_phone);
        tilEmail           = findViewById(R.id.til_email);
        tilPassword        = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilAddress         = findViewById(R.id.til_address);
        etName             = findViewById(R.id.et_name);
        etPhone            = findViewById(R.id.et_phone);
        etEmail            = findViewById(R.id.et_email);
        etPassword         = findViewById(R.id.et_password);
        etConfirmPassword  = findViewById(R.id.et_confirm_password);
        etAddress          = findViewById(R.id.et_address);
        btnRegister        = findViewById(R.id.btn_register);
        progressBar        = findViewById(R.id.progress_bar);
        tvError            = findViewById(R.id.tv_error);
        tvLoginLink        = findViewById(R.id.tv_login_link);

        // Back button
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        clearErrors();

        String name    = getText(etName);
        String phone   = getText(etPhone);
        String email   = getText(etEmail);
        String pass    = getText(etPassword);
        String confirm = getText(etConfirmPassword);
        String address = getText(etAddress);

        boolean valid = true;

        if (name.length() < 2) {
            tilName.setError("Họ tên phải có ít nhất 2 ký tự");
            valid = false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            tilPhone.setError("Số điện thoại không hợp lệ (VD: 0912345678)");
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui long nhap email");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email khong hop le");
            valid = false;
        }
        if (pass.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            valid = false;
        }
        if (!pass.equals(confirm)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            valid = false;
        }

        if (!valid) return;

        setLoading(true);

        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setPhone(phone);
        request.setEmail(email);
        request.setPassword(pass);
        request.setAddress(address.isEmpty() ? null : address);

        authViewModel.register(request).observe(this, result -> {
            switch (result.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    goToMain();
                    break;
                case ERROR:
                    setLoading(false);
                    showError(result.message);
                    break;
            }
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors() {
        tilName.setError(null);
        tilPhone.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tvError.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
