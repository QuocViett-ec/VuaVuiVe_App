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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.review.ReviewAdapter;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.CurrencyFormatter;
import vn.vuavuive.shared.util.Constants;

@AndroidEntryPoint
public class ProductDetailActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private Product currentProduct;
    private int quantity = 1;

    // Views
    private ImageView ivProduct;
    private TextView tvProductName, tvPrice, tvOriginalPrice, tvDiscount;
    private TextView tvRating, tvSold, tvStock, tvDescription, tvQuantity;
    private RatingBar ratingBar;
    private ImageButton btnDecrease, btnIncrease;
    private ExtendedFloatingActionButton fabAddToCart;
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
            loadProduct(productId);
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
            // Navigate to another ProductDetail
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
    }

    private void loadProduct(String productId) {
        productViewModel.getProductDetail(productId).observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                currentProduct = result.data;
                bindProduct(result.data);
                loadReviews(productId);
                loadSimilar(productId);
                productViewModel.sendRecommendEvent(Constants.EVENT_VIEW_PRODUCT, productId, null);
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindProduct(Product product) {
        // Collapsing toolbar title
        com.google.android.material.appbar.CollapsingToolbarLayout ctl =
                findViewById(R.id.collapsing_toolbar);
        if (ctl != null) ctl.setTitle(product.getName());

        // Image
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(ivProduct);

        tvProductName.setText(product.getName());

        // Price
        String priceUnit = CurrencyFormatter.format(product.getPrice())
                + (product.getUnit() != null ? "/" + product.getUnit() : "");
        tvPrice.setText(priceUnit);

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
        tvSold.setText("• " + sold + " đã bán");

        // Stock
        if (product.getStock() > 0) {
            tvStock.setText("Còn hàng: " + product.getStock() + (product.getUnit() != null ? " " + product.getUnit() : ""));
            tvStock.setTextColor(getResources().getColor(R.color.status_delivered, null));
            fabAddToCart.setEnabled(true);
        } else {
            tvStock.setText("Hết hàng");
            tvStock.setTextColor(getResources().getColor(R.color.error, null));
            fabAddToCart.setEnabled(false);
            fabAddToCart.setText("Hết hàng");
        }

        // Description
        tvDescription.setText(product.getDescription() != null
                ? product.getDescription() : "Chưa có mô tả sản phẩm.");
    }

    private void loadReviews(String productId) {
        productViewModel.getProductReviews(productId).observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                reviewAdapter.setReviews(result.data);
            }
        });
    }

    private void loadSimilar(String productId) {
        productViewModel.getSimilarProducts(productId).observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                similarAdapter.setProducts(result.data);
            }
        });
    }

    private void addToCart() {
        if (currentProduct == null) return;
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
        Toast.makeText(this, "Đã thêm " + quantity + " " +
                (currentProduct.getUnit() != null ? currentProduct.getUnit() : "sản phẩm") +
                " vào giỏ hàng", Toast.LENGTH_SHORT).show();

        java.util.Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("quantity", quantity);
        productViewModel.sendRecommendEvent(Constants.EVENT_ADD_TO_CART, currentProduct.getId(), meta);
    }
}
