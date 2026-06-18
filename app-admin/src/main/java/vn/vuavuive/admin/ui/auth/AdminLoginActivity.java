package vn.vuavuive.admin.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
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
            Toast.makeText(this, "Nhap email va mat khau", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLogin.setEnabled(false);
        authApi.adminLogin(new LoginRequest(email, password)).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                binding.btnLogin.setEnabled(true);
                ApiResponse<User> body = response.body();
                User user = body != null ? body.getData() : null;
                if (!response.isSuccessful() || body == null || !body.isSuccess() || user == null) {
                    Toast.makeText(AdminLoginActivity.this, "Dang nhap that bai", Toast.LENGTH_LONG).show();
                    return;
                }

                if (user.getRole() != null) user.setRole(user.getRole().toLowerCase());
                if (!user.isBackoffice() || !user.isActive()) {
                    Toast.makeText(AdminLoginActivity.this, "Tai khoan khong co quyen admin", Toast.LENGTH_LONG).show();
                    return;
                }

                sessionManager.saveUser(user);
                sessionManager.saveTokens(body.getAccessToken(), body.getRefreshToken());
                MockRepository.getInstance().setCurrentUser(user);
                startActivity(new Intent(AdminLoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                binding.btnLogin.setEnabled(true);
                Toast.makeText(AdminLoginActivity.this, "Loi ket noi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
