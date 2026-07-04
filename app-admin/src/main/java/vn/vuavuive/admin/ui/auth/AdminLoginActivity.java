package vn.vuavuive.admin.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.ActivityAdminLoginBinding;
import vn.vuavuive.admin.ui.main.MainActivity;
import vn.vuavuive.shared.data.api.AuthApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.request.LoginRequest;
import vn.vuavuive.shared.util.SessionManager;

@AndroidEntryPoint
public class AdminLoginActivity extends AppCompatActivity {
    @Inject AuthApi authApi;
    @Inject SessionManager sessionManager;

    private ActivityAdminLoginBinding binding;

    // Chỉ hiện 3 role hợp lệ cho backoffice — đã xoá "Customer" vì không có quyền đăng nhập
    private static final String[] ROLES_DISPLAY = {
            "Admin", "Staff", "Audit"
    };

    private static final String[] ROLES_EMAILS = {
            "admin@vuavuive.vn",
            "staff@vuavuive.vn",
            "audit@vuavuive.vn"
    };

    private static final String[] ROLES_PASSWORDS = {
            "Admin@123",
            "Staff@123",
            "Audit@123"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRoleSpinner();
        binding.btnLogin.setOnClickListener(v -> performLogin());
    }

    private void setupRoleSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ROLES_DISPLAY);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRole.setAdapter(adapter);
        binding.spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.etEmail.setText(ROLES_EMAILS[position]);
                binding.etPassword.setText(ROLES_PASSWORDS[position]);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void performLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLogin.setEnabled(false);
        LoginRequest request = new LoginRequest(email, password);
        authApi.adminLogin(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                binding.btnLogin.setEnabled(true);
                ApiResponse<User> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    User user = body.getData();
                    if (!user.isBackoffice()) {
                        Toast.makeText(AdminLoginActivity.this, "Tai khoan khong co quyen backoffice", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!sessionManager.saveSession(user, body.getAccessToken(), body.getRefreshToken())) {
                        Toast.makeText(AdminLoginActivity.this, "Phien dang nhap khong hop le", Toast.LENGTH_LONG).show();
                        return;
                    }
                    MockRepository.getInstance().setCurrentUser(user);
                    startActivity(new Intent(AdminLoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String errorMsg = "Đăng nhập thất bại. Kiểm tra lại thông tin!";
                    if (body != null && body.getMessage() != null) {
                        errorMsg = body.getMessage();
                    }
                    Toast.makeText(AdminLoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                binding.btnLogin.setEnabled(true);
                Toast.makeText(AdminLoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
