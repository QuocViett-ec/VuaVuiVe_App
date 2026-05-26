package vn.vuavuive.admin.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.ActivityAdminLoginBinding;
import vn.vuavuive.admin.ui.main.MainActivity;
import vn.vuavuive.shared.data.dto.User;

public class AdminLoginActivity extends AppCompatActivity {
    private ActivityAdminLoginBinding binding;

    private static final String[] ROLES_DISPLAY = {
            "Lê Hoàng Admin (Admin)",
            "Trần Thị Nhân Viên (Staff)",
            "Nguyễn Văn Kiểm Toán (Audit)",
            "Khách Hàng Thường (User - Bị từ chối)"
    };

    private static final String[] ROLES_EMAILS = {
            "admin@vuavuive.vn",
            "staff@vuavuive.vn",
            "audit@vuavuive.vn",
            "huy.pham@gmail.com"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRoleSpinner();

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }

    private void setupRoleSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ROLES_DISPLAY);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRole.setAdapter(adapter);

        binding.spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.etEmail.setText(ROLES_EMAILS[position]);
                binding.etPassword.setText("123456");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void performLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        MockRepository repo = MockRepository.getInstance();
        boolean success = repo.adminLogin(email, password);

        if (success) {
            User user = repo.getCurrentUser();
            Toast.makeText(this, "Đăng nhập thành công với quyền " + user.getRole().toUpperCase(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminLoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Đăng nhập thất bại! Chỉ chấp nhận tài khoản quản trị hoạt động.", Toast.LENGTH_LONG).show();
        }
    }
}
