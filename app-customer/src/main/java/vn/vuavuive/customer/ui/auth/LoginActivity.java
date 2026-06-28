package vn.vuavuive.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import vn.vuavuive.shared.util.SessionManager;
import javax.inject.Inject;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    @Inject SessionManager sessionManager;
    private AuthViewModel authViewModel;

    private TextInputLayout tilPhoneEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etPhoneEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvRegisterLink;
    private TextView tvForgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        setupClickListeners();
        showSessionExpiredMessageIfNeeded();

        // Check if already logged in (disabled by user request to always show login)
        // if (authViewModel.isLoggedIn()) {
        //     goToMain();
        //     return;
        // }
    }

    private void showSessionExpiredMessageIfNeeded() {
        if (getIntent().getBooleanExtra("session_expired", false)) {
            showError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
    }

    private void initViews() {
        tilPhoneEmail   = findViewById(R.id.til_phone_email);
        tilPassword     = findViewById(R.id.til_password);
        etPhoneEmail    = findViewById(R.id.et_phone_email);
        etPassword      = findViewById(R.id.et_password);
        btnLogin        = findViewById(R.id.btn_login);
        progressBar     = findViewById(R.id.progress_bar);
        tvError         = findViewById(R.id.tv_error);
        tvRegisterLink  = findViewById(R.id.tv_register_link);

        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // Pre-fill credentials for testing
        if (etPhoneEmail != null) {
            etPhoneEmail.setText("customer@gmail.com");
        }
        if (etPassword != null) {
            etPassword.setText("Customer@123");
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });


    }

    private void attemptLogin() {
        // Clear errors
        tilPhoneEmail.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);

        String phoneOrEmail = etPhoneEmail.getText() != null
                ? etPhoneEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString() : "";

        // Validate
        boolean valid = true;
        if (TextUtils.isEmpty(phoneOrEmail)) {
            tilPhoneEmail.setError("Vui lòng nhập số điện thoại hoặc email");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            valid = false;
        }
        if (!valid) return;

        setLoading(true);

        authViewModel.login(phoneOrEmail, password).observe(this, result -> {
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

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        if (sessionManager.isShipper()) {
            showError("Vui lòng sử dụng ứng dụng VuaVuiVe Shipper");
            sessionManager.clearSession();
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
