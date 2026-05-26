package vn.vuavuive.customer.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.MockDataProvider;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.data.dto.Product;
import java.util.List;

@AndroidEntryPoint
public class ProductListFragment extends Fragment {

    private ProductViewModel productViewModel;
    private ProductAdapter adapter;
    private boolean isLoading = false;
    private String currentCategory = "all";
    private String currentSearch = "";

    // Debounce search
    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        setupSearch(view);
        setupCategoryChips(view);
        setupRecyclerView(view);
        setupSwipeRefresh(view);
        loadProducts(view);
    }

    // ── Search setup ───────────────────────────────────────────────────────────
    private void setupSearch(View view) {
        TextInputEditText etSearch = view.findViewById(R.id.et_search);
        TextInputLayout tilSearch = view.findViewById(R.id.til_search);

        if (tilSearch != null) {
            tilSearch.setEndIconOnClickListener(v -> {
                String q = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
                currentSearch = q;
                loadProducts(view);
            });
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = () -> {
                        currentSearch = s.toString().trim();
                        productViewModel.setSearch(currentSearch);
                        loadProducts(view);
                    };
                    searchHandler.postDelayed(searchRunnable, 350);
                }
            });
        }
    }

    // ── Category chips ─────────────────────────────────────────────────────────
    private void setupCategoryChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_categories);
        if (chipGroup == null) return;

        for (String[] cat : MockDataProvider.CATEGORIES) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat[1]);
            chip.setCheckable(true);
            chip.setChecked("all".equals(cat[0]));
            chip.setChipBackgroundColorResource(R.color.surface_variant);
            chip.setTextColor(getResources().getColorStateList(R.color.bottom_nav_color, null));
            chip.setChipStrokeColorResource(R.color.outline);
            chip.setChipStrokeWidth(1f);
            chip.setOnClickListener(v -> {
                currentCategory = cat[0];
                productViewModel.setCategory(cat[0]);
                loadProducts(view);
            });
            chipGroup.addView(chip);
        }
    }

    // ── RecyclerView setup ─────────────────────────────────────────────────────
    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_products);
        if (rv == null) return;

        adapter = new ProductAdapter(getContext(), product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        adapter.setAddToCartListener(product -> {
            Toast.makeText(getContext(),
                    "✅ Đã thêm \"" + product.getName() + "\" vào giỏ",
                    Toast.LENGTH_SHORT).show();
        });

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);

        // Endless scroll pagination
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (!isLoading && dy > 0) {
                    int visible = layoutManager.getChildCount();
                    int total = layoutManager.getItemCount();
                    int first = layoutManager.findFirstVisibleItemPosition();
                    if (first + visible >= total - 4) {
                        loadNextPageFromApi(view);
                    }
                }
            }
        });
    }

    // ── Swipe refresh ──────────────────────────────────────────────────────────
    private void setupSwipeRefresh(View view) {
        SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.swipe_refresh);
        if (swipeRefresh == null) return;
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadProducts(view);
            swipeRefresh.setRefreshing(false);
        });
    }

    // ── Load products (mock first, then API) ───────────────────────────────────
    private void loadProducts(View view) {
        // First show mock data instantly for responsive UX
        List<Product> mockProducts = currentSearch.isEmpty()
                ? MockDataProvider.getMockProductsByCategory(currentCategory)
                : MockDataProvider.searchMockProducts(currentSearch);

        showLoading(view, false);
        if (adapter != null) {
            adapter.setProducts(mockProducts);
        }
        updateEmptyState(view, mockProducts.isEmpty(), currentSearch.isEmpty() ? null : currentSearch);

        // Then try API in background
        tryLoadFromApi(view);
    }

    private void tryLoadFromApi(View view) {
        try {
            isLoading = true;
            productViewModel.loadProducts(1).observe(getViewLifecycleOwner(), result -> {
                isLoading = false;
                if (result != null
                        && result.status == AuthRepository.Result.Status.SUCCESS
                        && result.data != null && !result.data.isEmpty()) {
                    if (adapter != null) adapter.setProducts(result.data);
                    updateEmptyState(view, false, null);
                }
                // Otherwise keep mock data showing
            });
        } catch (Exception e) {
            isLoading = false;
            // API not available, mock data is already shown
        }
    }

    private void loadNextPageFromApi(View view) {
        if (isLoading) return;
        isLoading = true;
        try {
            productViewModel.loadNextPage().observe(getViewLifecycleOwner(), result -> {
                isLoading = false;
                if (result != null
                        && result.status == AuthRepository.Result.Status.SUCCESS
                        && result.data != null && !result.data.isEmpty()) {
                    if (adapter != null) adapter.appendProducts(result.data);
                }
            });
        } catch (Exception e) {
            isLoading = false;
        }
    }

    // ── UI state helpers ───────────────────────────────────────────────────────
    private void showLoading(View view, boolean show) {
        if (view == null) return;
        LinearLayout layoutLoading = view.findViewById(R.id.layout_loading);
        if (layoutLoading != null) {
            layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState(View view, boolean isEmpty, String query) {
        if (view == null) return;
        LinearLayout layoutEmpty = view.findViewById(R.id.layout_empty);
        if (layoutEmpty == null) return;

        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (isEmpty) {
            TextView tvEmpty = view.findViewById(R.id.tv_empty);
            if (tvEmpty != null) {
                tvEmpty.setText(query != null
                        ? "Không tìm thấy \"" + query + "\""
                        : "Không có sản phẩm");
            }
        }

        // Wire up clear filter button
        View btnClear = view.findViewById(R.id.btn_clear_filter);
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> {
                currentCategory = "all";
                currentSearch = "";
                TextInputEditText etSearch = view.findViewById(R.id.et_search);
                if (etSearch != null) etSearch.setText("");
                loadProducts(view);
            });
        }
    }
}
