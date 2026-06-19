package vn.vuavuive.customer.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.local.CartItemEntity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@AndroidEntryPoint
public class FlashSaleActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private ProductAdapter productAdapter;

    // View bindings
    private View layoutActiveFlashSale;
    private View layoutInactiveFlashSale;
    private ProgressBar progressBar;
    private View layoutError;
    private TextView tvErrorMessage;
    private TextView tvCountdownActive;
    private TextView tvCountdownInactive;

    // Timer management
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private boolean wasActiveState = false; // To track status changes and reload layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_sale);

        // ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        initViews();
        setupToolbar();
        checkFlashSaleStatus();
        startTimer();
    }

    private void initViews() {
        layoutActiveFlashSale = findViewById(R.id.layout_active_flash_sale);
        layoutInactiveFlashSale = findViewById(R.id.layout_inactive_flash_sale);
        progressBar = findViewById(R.id.progress_bar);
        layoutError = findViewById(R.id.layout_error);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        tvCountdownActive = findViewById(R.id.tv_countdown_active);
        tvCountdownInactive = findViewById(R.id.tv_countdown_inactive);

        // Retry loading
        findViewById(R.id.btn_retry).setOnClickListener(v -> checkFlashSaleStatus());

        // Setup RecyclerView
        RecyclerView rvProducts = findViewById(R.id.rv_flash_sale_products);
        if (rvProducts != null) {
            productAdapter = new ProductAdapter(this, product -> {
                Intent intent = new Intent(FlashSaleActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            });
            productAdapter.setAddToCartListener(product -> {
                CartItemEntity item = new CartItemEntity();
                item.setProductId(product.getId());
                item.setProductName(product.getName() + " (Flash Sale)");
                item.setProductPrice(product.getPrice());
                item.setProductImageUrl(product.getImageUrl());
                item.setProductUnit(product.getUnit());
                item.setProductStock(product.getStock());
                item.setQuantity(1);
                item.setAddedAt(System.currentTimeMillis());
                item.setSavedForLater(false);
                cartViewModel.addItem(item);
                Toast.makeText(FlashSaleActivity.this,
                        "✅ Đã thêm \"" + product.getName() + "\" (Flash Sale) vào giỏ hàng",
                        Toast.LENGTH_SHORT).show();
            });
            rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
            rvProducts.setAdapter(productAdapter);
        }
    }

    private void setupToolbar() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void checkFlashSaleStatus() {
        boolean isActive = isFlashSaleActive();
        wasActiveState = isActive;

        if (isActive) {
            layoutInactiveFlashSale.setVisibility(View.GONE);
            layoutError.setVisibility(View.GONE);
            loadFlashSaleProducts();
        } else {
            layoutActiveFlashSale.setVisibility(View.GONE);
            layoutError.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            layoutInactiveFlashSale.setVisibility(View.VISIBLE);
            updateCountdownText(false);
        }
    }

    /**
     * Flash Sale is active:
     * - Morning: 06:00:00 to 07:59:59 (hour >= 6 and hour < 8)
     * - Afternoon: 14:00:00 to 15:59:59 (hour >= 14 and hour < 16)
     */
    private boolean isFlashSaleActive() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= 6 && hour < 8) || (hour >= 14 && hour < 16);
    }

    private void loadFlashSaleProducts() {
        progressBar.setVisibility(View.VISIBLE);
        layoutActiveFlashSale.setVisibility(View.GONE);

        // Fetch up to 100 products from API / cache
        productViewModel.getProducts(null, null, 1, 100, null)
                .observe(this, result -> {
                    progressBar.setVisibility(View.GONE);
                    if (result != null
                            && result.status == AuthRepository.Result.Status.SUCCESS
                            && result.data != null
                            && !result.data.isEmpty()) {

                        List<Product> flashSaleList = selectRandomFlashSaleProducts(result.data);
                        if (productAdapter != null) {
                            productAdapter.setProducts(flashSaleList);
                        }
                        layoutActiveFlashSale.setVisibility(View.VISIBLE);
                        layoutError.setVisibility(View.GONE);
                    } else {
                        String errMsg = (result != null && result.message != null)
                                ? result.message
                                : "Không tải được danh sách sản phẩm.";
                        tvErrorMessage.setText(errMsg);
                        layoutError.setVisibility(View.VISIBLE);
                        layoutActiveFlashSale.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Randomly selects 20 products from the full list.
     * Seeds the random generator with the current date + slot index,
     * ensuring the selection is persistent during the active 2-hour window.
     */
    private List<Product> selectRandomFlashSaleProducts(List<Product> allProducts) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int slotIndex = (hour >= 6 && hour < 8) ? 1 : 2;
        long seed = (long) year * 100000 + (long) month * 1000 + (long) day * 10 + slotIndex;

        Random seededRandom = new Random(seed);

        // Clone the list to shuffle safely
        List<Product> shuffled = new ArrayList<>(allProducts);
        Collections.shuffle(shuffled, seededRandom);

        int targetSize = Math.min(shuffled.size(), 20);
        List<Product> selected = new ArrayList<>(shuffled.subList(0, targetSize));

        // Apply a custom flash sale discount (25% - 50%) to the selected products
        for (Product p : selected) {
            // Backup current price as original price if it doesn't have one
            if (p.getOriginalPrice() == null || p.getOriginalPrice() <= p.getPrice()) {
                p.setOriginalPrice(p.getPrice());
            }
            int discountPct = 25 + seededRandom.nextInt(26); // 25% to 50%
            double discountedPrice = Math.round(p.getOriginalPrice() * (100 - discountPct) / 100.0);
            p.setPrice(discountedPrice);
        }

        return selected;
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                boolean activeNow = isFlashSaleActive();
                if (activeNow != wasActiveState) {
                    // Flash sale state has changed, reload page setup
                    checkFlashSaleStatus();
                } else {
                    updateCountdownText(activeNow);
                }
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void updateCountdownText(boolean isActive) {
        Calendar now = Calendar.getInstance();
        long currentMs = now.getTimeInMillis();

        if (isActive) {
            // Find end of active slot
            int endHour = (now.get(Calendar.HOUR_OF_DAY) >= 6 && now.get(Calendar.HOUR_OF_DAY) < 8) ? 8 : 16;
            Calendar endCal = (Calendar) now.clone();
            endCal.set(Calendar.HOUR_OF_DAY, endHour);
            endCal.set(Calendar.MINUTE, 0);
            endCal.set(Calendar.SECOND, 0);
            endCal.set(Calendar.MILLISECOND, 0);

            long diffMs = endCal.getTimeInMillis() - currentMs;
            if (diffMs < 0) diffMs = 0;

            String formattedTime = formatMillisToTime(diffMs);
            if (tvCountdownActive != null) {
                tvCountdownActive.setText(formattedTime);
            }
        } else {
            // Find start of next slot
            int hour = now.get(Calendar.HOUR_OF_DAY);
            Calendar nextCal = (Calendar) now.clone();
            nextCal.set(Calendar.MINUTE, 0);
            nextCal.set(Calendar.SECOND, 0);
            nextCal.set(Calendar.MILLISECOND, 0);

            if (hour < 6) {
                nextCal.set(Calendar.HOUR_OF_DAY, 6);
            } else if (hour >= 8 && hour < 14) {
                nextCal.set(Calendar.HOUR_OF_DAY, 14);
            } else {
                // Starts tomorrow at 6 AM
                nextCal.add(Calendar.DAY_OF_YEAR, 1);
                nextCal.set(Calendar.HOUR_OF_DAY, 6);
            }

            long diffMs = nextCal.getTimeInMillis() - currentMs;
            if (diffMs < 0) diffMs = 0;

            String formattedTime = formatMillisToTime(diffMs);
            if (tvCountdownInactive != null) {
                tvCountdownInactive.setText(formattedTime);
            }
        }
    }

    private String formatMillisToTime(long diffMs) {
        long hours = diffMs / (3600 * 1000);
        long minutes = (diffMs % (3600 * 1000)) / (60 * 1000);
        long seconds = (diffMs % (60 * 1000)) / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}
