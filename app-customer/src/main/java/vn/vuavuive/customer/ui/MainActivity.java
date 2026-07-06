package vn.vuavuive.customer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.shared.fcm.FcmTokenRegistrar;

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
        FcmTokenRegistrar.requestNotificationPermission(this);

        authViewModel.checkSession().observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS) {
                authViewModel.setCurrentUser(result.data);
                cartViewModel.onUserLoggedIn();
                FcmTokenRegistrar.register(
                        this,
                        "customer_promotions",
                        "customer_flash_sale",
                        "customer_news");
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                authViewModel.setCurrentUser(null);
            }
        });

        setupNavigation();
        observeCartCount();
        handleNavigateIntent(getIntent());
    }

    @Override
    protected void onNewIntent(@androidx.annotation.NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNavigateIntent(intent);
    }

    private void setupNavigation() {
        bottomNavView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            Toast.makeText(this, "Khong tim thay navigation host", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        navController = navHostFragment.getNavController();
        bottomNavView.setOnItemSelectedListener(item -> {
            navigateToDestination(item.getItemId());
            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.navigation_home
                    || id == R.id.navigation_products
                    || id == R.id.navigation_cart
                    || id == R.id.navigation_orders
                    || id == R.id.navigation_account) {
                if (bottomNavView.getSelectedItemId() != id) {
                    bottomNavView.getMenu().findItem(id).setChecked(true);
                }
            }
        });

        bottomNavView.setSelectedItemId(R.id.navigation_home);
    }

    private void handleNavigateIntent(Intent intent) {
        if (intent == null) return;
        String navigateTo = intent.getStringExtra("navigate_to");
        android.net.Uri data = intent.getData();
        boolean ordersDeepLink = data != null
                && "vuavuive".equals(data.getScheme())
                && "orders".equals(data.getHost());
        if (("orders".equals(navigateTo) || ordersDeepLink) && bottomNavView != null) {
            bottomNavView.setSelectedItemId(R.id.navigation_orders);
            intent.removeExtra("navigate_to");
        }
    }

    private void navigateToDestination(@IdRes int destinationId) {
        if (navController == null) return;
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == destinationId) {
            return;
        }

        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.getGraph().getStartDestinationId(), false, true)
                .build();
        navController.navigate(destinationId, null, options);
    }

    private void observeCartCount() {
        cartViewModel.getCartCount().observe(this, count -> {
            if (bottomNavView == null) return;
            if (count != null && count > 0) {
                bottomNavView.getOrCreateBadge(R.id.navigation_cart).setNumber(count);
            } else {
                bottomNavView.removeBadge(R.id.navigation_cart);
            }
        });
    }

    public void navigateToProducts() {
        if (bottomNavView != null) {
            bottomNavView.setSelectedItemId(R.id.navigation_products);
        } else {
            navigateToDestination(R.id.navigation_products);
        }
    }

}
