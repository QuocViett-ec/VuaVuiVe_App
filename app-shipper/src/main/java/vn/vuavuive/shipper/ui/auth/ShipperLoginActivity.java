package vn.vuavuive.shipper.ui.auth;

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
import javax.inject.Inject;
import vn.vuavuive.shared.util.SessionManager;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shipper.ui.main.ShipperMainActivity;
import vn.vuavuive.shipper.viewmodel.AuthViewModel;

@AndroidEntryPoint
public class ShipperLoginActivity extends AppCompatActivity {
    @Inject SessionManager sessionManager;

    private AuthViewModel authViewModel;
    private TextInputLayout tilIdentifier;
    private TextInputLayout tilPassword;
    private TextInputEditText etIdentifier;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_login);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        tilIdentifier = findViewById(R.id.til_identifier);
        tilPassword = findViewById(R.id.til_password);
        etIdentifier = findViewById(R.id.et_identifier);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvError = findViewById(R.id.tv_error);

        // TODO: Prefill for testing (remove in production)
        etIdentifier.setText("shipper@gmail.com");
        etPassword.setText("Shipper@123");

        if (authViewModel.isLoggedIn()) {
            goToMain();
            return;
        }
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        tilIdentifier.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);
        String identifier = etIdentifier.getText() != null ? etIdentifier.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        boolean valid = true;
        if (TextUtils.isEmpty(identifier)) {
            tilIdentifier.setError("Nhap so dien thoai hoac email");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Nhap mat khau");
            valid = false;
        }
        if (!valid) return;

        setLoading(true);
        authViewModel.login(identifier, password).observe(this, result -> {
            switch (result.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    if (sessionManager.isShipper()) {
                        goToMain();
                    } else {
                        sessionManager.clearSession();
                        showError("Tai khoan khong co quyen Shipper");
                    }
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
    }

    private void showError(String message) {
        tvError.setText(message != null ? message : "Dang nhap that bai");
        tvError.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        Intent intent = new Intent(this, ShipperMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
