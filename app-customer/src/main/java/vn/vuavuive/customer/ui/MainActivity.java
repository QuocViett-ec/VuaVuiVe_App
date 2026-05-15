package vn.vuavuive.customer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.ui.auth.LoginActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.customer.viewmodel.CartViewModel;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private CartViewModel cartViewModel;
    private BottomNavigationView bottomNavView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // Check session
        authViewModel.checkSession().observe(this, result -> {
            if (result.status == vn.vuavuive.customer.data.repository.AuthRepository.Result.Status.SUCCESS) {
                authViewModel.setCurrentUser(result.data);
                // Sync cart from server
                cartViewModel.syncFromServer();
            } else if (result.status == vn.vuavuive.customer.data.repository.AuthRepository.Result.Status.ERROR) {
                // Not logged in — go to login
                goToLogin();
            }
        });

        setupNavigation();
        observeCartCount();

        // Handle deep-link từ PaymentWebViewActivity
        handleNavigateIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNavigateIntent(intent);
    }

    private void handleNavigateIntent(Intent intent) {
        if (intent == null) return;
        String navigateTo = intent.getStringExtra("navigate_to");
        if ("orders".equals(navigateTo) && navController != null) {
            navController.navigate(R.id.navigation_orders);
        }
    }

    private void setupNavigation() {
        bottomNavView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavView, navController);
        }
    }

    private void observeCartCount() {
        cartViewModel.getCartCount().observe(this, count -> {
            if (count != null && count > 0) {
                bottomNavView.getOrCreateBadge(R.id.navigation_cart).setNumber(count);
            } else {
                bottomNavView.removeBadge(R.id.navigation_cart);
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
