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

    private static final String[] ROLES_DISPLAY = {
            "Admin", "Staff", "Audit", "Customer (blocked)"
    };

    private static final String[] ROLES_EMAILS = {
            "admin@vuavuive.vn",
            "staff@vuavuive.vn",
            "audit@vuavuive.vn",
            "customer@gmail.com"
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
                binding.etPassword.setText(position == 0 ? "Admin@123" : "123456");
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
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    User user = response.body().getData();
                    String accessToken = response.body().getAccessToken();
                    String refreshToken = response.body().getRefreshToken();
                    
                    if (accessToken != null) {
                        sessionManager.saveTokens(accessToken, refreshToken != null ? refreshToken : "");
                    }
                    sessionManager.saveUser(user);
                    MockRepository.getInstance().setCurrentUser(user);
                    startActivity(new Intent(AdminLoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(AdminLoginActivity.this, "Đăng nhập thất bại. Kiểm tra lại thông tin!", Toast.LENGTH_SHORT).show();
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
