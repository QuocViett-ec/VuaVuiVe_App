package vn.vuavuive.customer.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.MockDataProvider;
import vn.vuavuive.customer.ui.product.ProductAdapter;
import vn.vuavuive.customer.ui.product.ProductDetailActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.shared.data.dto.Product;
import java.util.List;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private AuthViewModel authViewModel;
    private ProductAdapter featuredAdapter;
    private ProductAdapter saleAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupGreeting(view);
        setupQuickCategories(view);
        setupFeaturedProducts(view);
        setupSaleProducts(view);
        loadMockData();

        // See all click
        TextView tvSeeAll = view.findViewById(R.id.tv_see_all);
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> navigateToProducts());
        }
        TextView tvSeeAllSale = view.findViewById(R.id.tv_see_all_sale);
        if (tvSeeAllSale != null) {
            tvSeeAllSale.setOnClickListener(v -> navigateToProducts());
        }
    }

    // ── Setup greeting ─────────────────────────────────────────────────────────
    private void setupGreeting(View view) {
        TextView tvGreeting = view.findViewById(R.id.tv_greeting_name);
        if (tvGreeting == null) return;

        try {
            String name = authViewModel.getCurrentUser().getValue() != null
                    ? authViewModel.getCurrentUser().getValue().getName() : null;
            tvGreeting.setText(name != null && !name.isEmpty() ? name : "Vựa Vui Vẻ");
        } catch (Exception e) {
            tvGreeting.setText("Vựa Vui Vẻ");
        }
    }

    // ── Setup quick categories ─────────────────────────────────────────────────
    private void setupQuickCategories(View view) {
        LinearLayout llCategories = view.findViewById(R.id.ll_quick_categories);
        if (llCategories == null) return;

        String[][] categories = MockDataProvider.CATEGORIES;
        for (String[] cat : categories) {
            TextView chip = (TextView) LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_category_chip, llCategories, false);
            chip.setText(cat[1]);
            chip.setOnClickListener(v -> navigateToProducts());
            llCategories.addView(chip);
        }
    }

    // ── Setup featured products RecyclerView ───────────────────────────────────
    private void setupFeaturedProducts(View view) {
        RecyclerView rvFeatured = view.findViewById(R.id.rv_featured_products);
        if (rvFeatured == null) return;

        featuredAdapter = new ProductAdapter(getContext(), this::openProductDetail);
        featuredAdapter.setAddToCartListener(this::quickAddToCart);
        rvFeatured.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeatured.setAdapter(featuredAdapter);
    }

    // ── Setup sale products RecyclerView ───────────────────────────────────────
    private void setupSaleProducts(View view) {
        RecyclerView rvSale = view.findViewById(R.id.rv_sale_products);
        if (rvSale == null) return;

        saleAdapter = new ProductAdapter(getContext(), this::openProductDetail);
        saleAdapter.setAddToCartListener(this::quickAddToCart);
        rvSale.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSale.setAdapter(saleAdapter);
    }

    // ── Load mock data ─────────────────────────────────────────────────────────
    private void loadMockData() {
        // Featured products
        List<Product> featured = MockDataProvider.getMockFeaturedProducts();
        if (featuredAdapter != null) featuredAdapter.setProducts(featured);

        // Sale products
        List<Product> saleProducts = MockDataProvider.getMockSaleProducts();
        if (saleAdapter != null) saleAdapter.setProducts(saleProducts);

        // Try API data as well (if available), falls back to mock silently
        tryLoadFromApi();
    }

    private void tryLoadFromApi() {
        try {
            vn.vuavuive.customer.viewmodel.ProductViewModel productViewModel =
                    new ViewModelProvider(requireActivity())
                            .get(vn.vuavuive.customer.viewmodel.ProductViewModel.class);

            productViewModel.loadProducts(1).observe(getViewLifecycleOwner(), result -> {
                if (result != null
                        && result.status == vn.vuavuive.customer.data.repository.AuthRepository.Result.Status.SUCCESS
                        && result.data != null && !result.data.isEmpty()) {
                    if (featuredAdapter != null) featuredAdapter.setProducts(result.data);
                }
                // If error or empty, keep mock data (already loaded)
            });
        } catch (Exception e) {
            // Network not available — mock data already shown, ignore
        }
    }

    // ── Navigation helpers ─────────────────────────────────────────────────────
    private void openProductDetail(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    private void navigateToProducts() {
        // Navigate to product list tab
        try {
            if (getActivity() instanceof vn.vuavuive.customer.ui.MainActivity) {
                ((vn.vuavuive.customer.ui.MainActivity) getActivity()).navigateToProducts();
            }
        } catch (Exception ignored) {}
    }

    private void quickAddToCart(Product product) {
        Toast.makeText(getContext(),
                "✅ Đã thêm \"" + product.getName() + "\" vào giỏ hàng",
                Toast.LENGTH_SHORT).show();
    }
}
