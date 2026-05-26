package vn.vuavuive.admin.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationBarView;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.ActivityMainBinding;
import vn.vuavuive.admin.ui.auth.AdminLoginActivity;
import vn.vuavuive.admin.ui.chatbot.AdminChatFragment;
import vn.vuavuive.admin.ui.dashboard.DashboardFragment;
import vn.vuavuive.admin.ui.orders.AdminOrderListFragment;
import vn.vuavuive.admin.ui.products.AdminProductListFragment;
import vn.vuavuive.admin.ui.vouchers.VoucherListFragment;
import vn.vuavuive.shared.data.dto.User;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = MockRepository.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Force login if session is empty
            logout();
            return;
        }

        setupUI();
    }

    private void setupUI() {
        // Setup Toolbar
        binding.tvRoleBadge.setText(currentUser.getRole().toUpperCase());
        binding.ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Hide menus based on roles (if needed, but default bottom nav items fit staff/admin/audit well)
        // Audit role: can read products & orders but cannot modify.
        // Let's configure bottom nav actions
        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment fragment = null;

                if (id == R.id.nav_dashboard) {
                    fragment = new DashboardFragment();
                } else if (id == R.id.nav_orders) {
                    fragment = new AdminOrderListFragment();
                } else if (id == R.id.nav_products) {
                    fragment = new AdminProductListFragment();
                } else if (id == R.id.nav_vouchers) {
                    fragment = new VoucherListFragment();
                } else if (id == R.id.nav_chatbot) {
                    fragment = new AdminChatFragment();
                }

                if (fragment != null) {
                    replaceFragment(fragment);
                    return true;
                }
                return false;
            }
        });

        // Set default fragment
        binding.bottomNav.setSelectedItemId(R.id.nav_dashboard);
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateToMenu(int menuId) {
        binding.bottomNav.setSelectedItemId(menuId);
    }

    private void logout() {
        MockRepository.getInstance().setCurrentUser(null);
        Toast.makeText(this, "Đã đăng xuất hệ thống", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, AdminLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
