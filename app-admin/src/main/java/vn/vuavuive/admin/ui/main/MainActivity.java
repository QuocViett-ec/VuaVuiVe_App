package vn.vuavuive.admin.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.navigation.NavigationBarView;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
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
import vn.vuavuive.shared.fcm.FcmTokenRegistrar;
import vn.vuavuive.shared.util.SessionManager;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject SessionManager sessionManager;

    private ActivityMainBinding binding;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FcmTokenRegistrar.requestNotificationPermission(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isBackoffice()) {
            logout();
            return;
        }
        currentUser = sessionManager.getUser();
        if (currentUser == null) {
            logout();
            return;
        }
        MockRepository.getInstance().setCurrentUser(currentUser);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().getCurrentUser().getIdToken(false)
                    .addOnSuccessListener(token -> sessionManager.saveTokens(token.getToken(), null));
        }
        setupUI();
        FcmTokenRegistrar.register(this, "admin_orders");
    }

    private void setupUI() {
        String role = currentUser.getRole();
        binding.tvRoleBadge.setText(role != null ? role.toUpperCase() : "BACKOFFICE");
        binding.ivLogout.setOnClickListener(v -> logout());
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
                if (fragment == null) return false;
                replaceFragment(fragment);
                return true;
            }
        });
        binding.bottomNav.setSelectedItemId(R.id.nav_dashboard);
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    public void navigateToMenu(int menuId) {
        binding.bottomNav.setSelectedItemId(menuId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing()) return;
        if (sessionManager == null) return;
        if (!sessionManager.isLoggedIn() || !sessionManager.isBackoffice()) {
            logout();
        }
    }

    private void logout() {
        MockRepository.getInstance().setCurrentUser(null);
        sessionManager.clearSession();
        Toast.makeText(this, "Da dang xuat", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, AdminLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
