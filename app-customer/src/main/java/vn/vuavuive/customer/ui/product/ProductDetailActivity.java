package vn.vuavuive.customer.ui.product;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import vn.vuavuive.customer.R;
import vn.vuavuive.shared.util.SessionManager;
import vn.vuavuive.customer.data.MockDataProvider;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.review.ReviewAdapter;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.Review;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.CurrencyFormatter;
import vn.vuavuive.shared.util.Constants;
import java.util.List;

@AndroidEntryPoint
public class ProductDetailActivity extends AppCompatActivity {

    @Inject
    SessionManager sessionManager;

    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private Product currentProduct;
    private int quantity = 1;

    // Views
    private ImageView ivProduct;
    private TextView tvProductName, tvPrice, tvOriginalPrice, tvDiscount;
    private TextView tvRating, tvSold, tvStock, tvDescription, tvQuantity;
    private RatingBar ratingBar;
    private ImageView btnDecrease, btnIncrease;
    private MaterialButton fabAddToCart;
    private RecyclerView rvReviews, rvSimilarProducts;
    private ReviewAdapter reviewAdapter;
    private ProductAdapter similarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel    = new ViewModelProvider(this).get(CartViewModel.class);

        setupToolbar();
        initViews();

        String productId = getIntent().getStringExtra("product_id");
        if (productId != null) {
            // Load mock data instantly for responsive UX
            Product mockProduct = MockDataProvider.getMockProductById(productId);
            if (mockProduct != null) {
                currentProduct = mockProduct;
                bindProduct(mockProduct);
                loadMockReviews(productId);
                loadMockSimilarProducts(productId);
            }

            // Then try to load from API (non-blocking)
            tryLoadFromApi(productId);
        } else {
            finish();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        ivProduct        = findViewById(R.id.iv_product);
        tvProductName    = findViewById(R.id.tv_product_name);
        tvPrice          = findViewById(R.id.tv_price);
        tvOriginalPrice  = findViewById(R.id.tv_original_price);
        tvDiscount       = findViewById(R.id.tv_discount);
        ratingBar        = findViewById(R.id.rating_bar);
        tvRating         = findViewById(R.id.tv_rating);
        tvSold           = findViewById(R.id.tv_sold);
        tvStock          = findViewById(R.id.tv_stock);
        tvDescription    = findViewById(R.id.tv_description);
        tvQuantity       = findViewById(R.id.tv_quantity);
        btnDecrease      = findViewById(R.id.btn_decrease);
        btnIncrease      = findViewById(R.id.btn_increase);
        fabAddToCart     = findViewById(R.id.fab_add_to_cart);
        rvReviews        = findViewById(R.id.rv_reviews);
        rvSimilarProducts = findViewById(R.id.rv_similar_products);

        // Reviews RecyclerView
        reviewAdapter = new ReviewAdapter(this);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
        rvReviews.setNestedScrollingEnabled(false);

        // Similar Products RecyclerView (horizontal)
        similarAdapter = new ProductAdapter(this, product -> {
            android.content.Intent intent = new android.content.Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        rvSimilarProducts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSimilarProducts.setAdapter(similarAdapter);
        rvSimilarProducts.setNestedScrollingEnabled(false);

        // Quantity controls
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });
        btnIncrease.setOnClickListener(v -> {
            int maxStock = currentProduct != null ? currentProduct.getStock() : 99;
            if (quantity < maxStock) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
            }
        });

        // FAB
        fabAddToCart.setOnClickListener(v -> addToCart());

        // Write Review button
        TextView btnWriteReview = findViewById(R.id.btn_write_review);
        if (btnWriteReview != null) {
            btnWriteReview.setOnClickListener(v -> {
                if (sessionManager == null || !sessionManager.isLoggedIn()) {
                    Toast.makeText(this, "Vui lòng đăng nhập để viết đánh giá", Toast.LENGTH_SHORT).show();
                    android.content.Intent intent = new android.content.Intent(this, vn.vuavuive.customer.ui.auth.LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                if (currentProduct != null) {
                    vn.vuavuive.customer.ui.review.ReviewBottomSheetDialogFragment dialog = 
                            vn.vuavuive.customer.ui.review.ReviewBottomSheetDialogFragment.newInstance(
                                    "",
                                    currentProduct.getId(),
                                    currentProduct.getName(),
                                    currentProduct.getImageUrl(),
                                    currentProduct.getPrice(),
                                    currentProduct.getUnit()
                            );
                    dialog.show(getSupportFragmentManager(), "ReviewBottomSheet");
                }
            });
        }
    }

    // ── Mock data loading ──────────────────────────────────────────────────────
    private void loadMockReviews(String productId) {
        List<Review> reviews = MockDataProvider.getMockReviews(productId);
        if (reviewAdapter != null) reviewAdapter.setReviews(reviews);
    }

    private void loadMockSimilarProducts(String productId) {
        // Get products from same category as the current product
        String category = currentProduct != null ? currentProduct.getCategory() : "veg";
        List<Product> similar = MockDataProvider.getMockProductsByCategory(category);
        // Remove current product
        similar.removeIf(p -> p.getId() != null && p.getId().equals(productId));
        if (similarAdapter != null) similarAdapter.setProducts(similar);
    }

    // ── API loading (non-blocking, enhances mock if available) ─────────────────
    private void tryLoadFromApi(String productId) {
        try {
            productViewModel.getProductDetail(productId).observe(this, result -> {
                if (result != null
                        && result.status == AuthRepository.Result.Status.SUCCESS
                        && result.data != null) {
                    currentProduct = result.data;
                    bindProduct(result.data);
                    try {
                        productViewModel.sendRecommendEvent(Constants.EVENT_VIEW_PRODUCT, productId, null);
                    } catch (Exception ignored) {}
                }
            });

            refreshReviews(productId);

            productViewModel.getSimilarProducts(productId).observe(this, result -> {
                if (result != null
                        && result.status == AuthRepository.Result.Status.SUCCESS
                        && result.data != null && !result.data.isEmpty()) {
                    similarAdapter.setProducts(result.data);
                }
            });
        } catch (Exception e) {
            // API unavailable — mock data already shown
        }
    }

    public void refreshReviews() {
        if (currentProduct == null) return;
        String productId = currentProduct.getId();
        if (productId == null || productId.isEmpty()) return;
        refreshReviews(productId);
    }

    private void refreshReviews(String productId) {
        try {
            productViewModel.getProductReviews(productId).observe(this, result -> {
                if (result != null
                        && result.status == AuthRepository.Result.Status.SUCCESS
                        && result.data != null) {
                    reviewAdapter.setReviews(result.data);
                }
            });
        } catch (Exception ignored) {}
    }

    // ── Bind product data to views ─────────────────────────────────────────────
    private void bindProduct(Product product) {
        // Collapsing toolbar title
        com.google.android.material.appbar.CollapsingToolbarLayout ctl =
                findViewById(R.id.collapsing_toolbar);
        if (ctl != null) ctl.setTitle(product.getName());

        // Image
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .centerCrop()
                .into(ivProduct);

        tvProductName.setText(product.getName());

        // Price
        String priceUnit = CurrencyFormatter.format(product.getPrice())
                + (product.getUnit() != null ? "/" + product.getUnit() : "");
        tvPrice.setText(priceUnit);

        // Original price & discount
        if (product.getOriginalPrice() != null && product.getOriginalPrice() > product.getPrice()) {
            tvOriginalPrice.setText(CurrencyFormatter.format(product.getOriginalPrice()));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPrice.setVisibility(View.VISIBLE);
            int pct = (int) ((1 - product.getPrice() / product.getOriginalPrice()) * 100);
            tvDiscount.setText("-" + pct + "%");
            tvDiscount.setVisibility(View.VISIBLE);
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscount.setVisibility(View.GONE);
        }

        // Rating
        float rating = product.getRating() != null ? product.getRating().floatValue() : 0f;
        ratingBar.setRating(rating);
        int reviewCount = product.getReviewCount() != null ? product.getReviewCount() : 0;
        tvRating.setText(String.format("%.1f (%d đánh giá)", rating, reviewCount));

        // Sold
        int sold = product.getSoldCount() != null ? product.getSoldCount() : 0;
        tvSold.setText("• " + (sold >= 1000 ? (sold / 1000) + "K" : sold) + " đã bán");

        // Stock status
        if (product.getStock() > 0) {
            if (quantity > product.getStock()) quantity = product.getStock();
            if (quantity < 1) quantity = 1;
            tvQuantity.setText(String.valueOf(quantity));
            String stockText = product.getStock() >= 1000
                    ? "Còn hàng: " + (product.getStock() / 1000) + "K+ " + (product.getUnit() != null ? product.getUnit() : "")
                    : "Còn hàng: " + product.getStock() + (product.getUnit() != null ? " " + product.getUnit() : "");
            tvStock.setText(stockText);
            tvStock.setTextColor(getResources().getColor(R.color.status_delivered, null));
            fabAddToCart.setEnabled(true);
            fabAddToCart.setText(getString(R.string.btn_add_to_cart));
        } else {
            tvStock.setText("Hết hàng");
            tvStock.setTextColor(getResources().getColor(R.color.error, null));
            fabAddToCart.setEnabled(false);
            quantity = 1;
            tvQuantity.setText(String.valueOf(quantity));
            fabAddToCart.setText("Hết hàng");
        }

        // Description
        tvDescription.setText(product.getDescription() != null
                ? product.getDescription() : "Chưa có mô tả sản phẩm.");
    }

    private void addToCart() {
        if (currentProduct == null) {
            Toast.makeText(this, "Đang tải thông tin sản phẩm, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentProduct.getId() == null || currentProduct.getId().startsWith("11111111") || currentProduct.getId().startsWith("mock_")) {
            Toast.makeText(this, "Không thể mua sản phẩm thử nghiệm", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentProduct.getStock() <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        CartItemEntity item = new CartItemEntity();
        item.setProductId(currentProduct.getId());
        item.setQuantity(quantity);
        item.setProductName(currentProduct.getName());
        item.setProductPrice(currentProduct.getPrice());
        item.setProductImageUrl(currentProduct.getImageUrl());
        item.setProductUnit(currentProduct.getUnit());
        item.setProductStock(currentProduct.getStock());
        item.setAddedAt(System.currentTimeMillis());
        item.setSavedForLater(false);

        cartViewModel.addItem(item);

        String unit = currentProduct.getUnit() != null ? currentProduct.getUnit() : "sản phẩm";
        Toast.makeText(this,
                "✅ Đã thêm " + quantity + " " + unit + " vào giỏ hàng",
                Toast.LENGTH_SHORT).show();

        try {
            java.util.Map<String, Object> meta = new java.util.HashMap<>();
            meta.put("quantity", quantity);
            productViewModel.sendRecommendEvent(Constants.EVENT_ADD_TO_CART, currentProduct.getId(), meta);
        } catch (Exception ignored) {}
    }
}
