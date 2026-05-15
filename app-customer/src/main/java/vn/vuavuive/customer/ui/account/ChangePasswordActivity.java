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

@AndroidEntryPoint
public class ChangePasswordActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private TextInputLayout tilOldPass, tilNewPass, tilConfirmPass;
    private TextInputEditText etOldPass, etNewPass, etConfirmPass;
    private MaterialButton btnChange;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        initViews();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tilOldPass     = findViewById(R.id.til_old_password);
        tilNewPass     = findViewById(R.id.til_new_password);
        tilConfirmPass = findViewById(R.id.til_confirm_password);
        etOldPass      = findViewById(R.id.et_old_password);
        etNewPass      = findViewById(R.id.et_new_password);
        etConfirmPass  = findViewById(R.id.et_confirm_password);
        btnChange      = findViewById(R.id.btn_change);
        progressBar    = findViewById(R.id.progress_bar);
        tvError        = findViewById(R.id.tv_error);

        btnChange.setOnClickListener(v -> attemptChange());
    }

    private void attemptChange() {
        String oldPass = getText(etOldPass);
        String newPass = getText(etNewPass);
        String confirm = getText(etConfirmPass);

        tilOldPass.setError(null);
        tilNewPass.setError(null);
        tilConfirmPass.setError(null);

        if (oldPass.isEmpty()) {
            tilOldPass.setError("Vui lòng nhập mật khẩu hiện tại");
            return;
        }
        if (newPass.length() < 6) {
            tilNewPass.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }
        if (!newPass.equals(confirm)) {
            tilConfirmPass.setError("Xác nhận mật khẩu không khớp");
            return;
        }

        setLoading(true);
        authViewModel.changePassword(oldPass, newPass).observe(this, result -> {
            setLoading(false);
            if (result.status == AuthRepository.Result.Status.SUCCESS) {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                tvError.setText(result.message);
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        btnChange.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
