package vn.vuavuive.customer.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
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
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.CategoryViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.data.dto.CategoryResponse;
import vn.vuavuive.shared.data.dto.Product;
import java.util.List;

@AndroidEntryPoint
public class ProductListFragment extends Fragment {

    private ProductViewModel productViewModel;
    private CategoryViewModel categoryViewModel;
    private ProductAdapter adapter;
    private boolean isLoading = false;
    private String currentCategory = "all";
    private String currentSearch = "";
    private TextInputEditText etSearch;

    // Debounce search
    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    private LiveData<AuthRepository.Result<List<Product>>> productsLiveData;
    private LiveData<AuthRepository.Result<List<Product>>> nextPageLiveData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productViewModel  = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        setupSearch(view);
        setupCategoryChips(view);
        setupRecyclerView(view);
        setupSwipeRefresh(view);
        loadProducts(view);
    }

    // ── Search ─────────────────────────────────────────────────────────────────
    private void setupSearch(View view) {
        etSearch = view.findViewById(R.id.et_search);
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

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (clearSearch(view)) return;
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }

    private boolean clearSearch(View view) {
        if (etSearch == null || (!etSearch.hasFocus() && currentSearch.isEmpty())) return false;
        searchHandler.removeCallbacks(searchRunnable);
        if (etSearch.getText() != null && etSearch.getText().length() > 0) {
            etSearch.setText("");
            currentSearch = "";
            productViewModel.setSearch("");
            loadProducts(view);
        }
        etSearch.clearFocus();
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        return true;
    }

    // ── Category chips — loaded from DB only, no mock ──────────────────────────
    private void setupCategoryChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_categories);
        if (chipGroup == null) return;

        if (categoryViewModel == null) {
            // DB unavailable — show "Tất cả" chip alone
            buildChipsFromApi(view, chipGroup, null);
            return;
        }

        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), result -> {
            if (result != null
                    && result.status == AuthRepository.Result.Status.SUCCESS
                    && result.data != null
                    && !result.data.isEmpty()) {
                buildChipsFromApi(view, chipGroup, result.data);
            } else {
                // API empty/error — show "Tất cả" chip alone so products still load
                buildChipsFromApi(view, chipGroup, null);
            }
        });
    }

    /** Builds category chips from live DB data. Pass null to get only the "Tất cả" chip. */
    private void buildChipsFromApi(View view, ChipGroup chipGroup, List<CategoryResponse> categories) {
        chipGroup.removeAllViews();

        // "Tất cả" first
        Chip allChip = new Chip(requireContext());
        allChip.setText("🛒 Tất cả");
        allChip.setCheckable(true);
        allChip.setChecked("all".equals(currentCategory));
        allChip.setChipBackgroundColorResource(R.color.surface_variant);
        allChip.setTextColor(getResources().getColorStateList(R.color.bottom_nav_color, null));
        allChip.setChipStrokeColorResource(R.color.outline);
        allChip.setChipStrokeWidth(1f);
        allChip.setOnClickListener(v -> {
            currentCategory = "all";
            productViewModel.setCategory("all");
            loadProducts(view);
        });
        chipGroup.addView(allChip);

        if (categories == null) return;

        for (CategoryResponse cat : categories) {
            String slug = cat.getSlug();
            String label = getEmoji(slug) + " " + cat.getName();
            Chip chip = new Chip(requireContext());
            chip.setText(label);
            chip.setCheckable(true);
            chip.setChecked(slug.equals(currentCategory));
            chip.setChipBackgroundColorResource(R.color.surface_variant);
            chip.setTextColor(getResources().getColorStateList(R.color.bottom_nav_color, null));
            chip.setChipStrokeColorResource(R.color.outline);
            chip.setChipStrokeWidth(1f);
            chip.setOnClickListener(v -> {
                currentCategory = slug;
                productViewModel.setCategory(slug);
                loadProducts(view);
            });
            chipGroup.addView(chip);
        }
    }

    private String getEmoji(String slug) {
        if (slug == null) return "🏷️";
        switch (slug) {
            case "veg":       return "🥦";
            case "fruit":     return "🍎";
            case "meat":      return "🥩";
            case "drink":     return "🥤";
            case "dry":       return "🌾";
            case "spice":     return "🌶️";
            case "sweet":     return "🍰";
            case "frozen":    return "❄️";
            case "household": return "🏠";
            default:          return "🏷️";
        }
    }

    // ── RecyclerView ───────────────────────────────────────────────────────────
    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_products);
        if (rv == null) return;

        adapter = new ProductAdapter(getContext(), product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        adapter.setAddToCartListener(product ->
                Toast.makeText(getContext(),
                        "✅ Đã thêm \"" + product.getName() + "\" vào giỏ",
                        Toast.LENGTH_SHORT).show());

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);

        // Endless scroll pagination
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (!isLoading && dy > 0) {
                    int visible = layoutManager.getChildCount();
                    int total   = layoutManager.getItemCount();
                    int first   = layoutManager.findFirstVisibleItemPosition();
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

    // ── Load products from API only — no mock data ─────────────────────────────
    private void loadProducts(View view) {
        showLoading(view, true);
        try {
            isLoading = true;
            if (productsLiveData != null) {
                productsLiveData.removeObservers(getViewLifecycleOwner());
            }
            productsLiveData = productViewModel.loadProducts(1);
            productsLiveData.observe(getViewLifecycleOwner(), result -> {
                isLoading = false;
                showLoading(view, false);
                if (result != null
                        && result.status == AuthRepository.Result.Status.SUCCESS
                        && result.data != null) {
                    if (adapter != null) adapter.setProducts(result.data);
                    updateEmptyState(view, result.data.isEmpty(), currentSearch.isEmpty() ? null : currentSearch);
                } else {
                    updateEmptyState(view, true, currentSearch.isEmpty() ? null : currentSearch);
                }
            });
        } catch (Exception e) {
            isLoading = false;
            showLoading(view, false);
        }
    }

    private void loadNextPageFromApi(View view) {
        if (isLoading) return;
        isLoading = true;
        try {
            if (nextPageLiveData != null) {
                nextPageLiveData.removeObservers(getViewLifecycleOwner());
            }
            nextPageLiveData = productViewModel.loadNextPage();
            nextPageLiveData.observe(getViewLifecycleOwner(), result -> {
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
        if (layoutLoading != null) layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
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

        View btnClear = view.findViewById(R.id.btn_clear_filter);
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> {
                currentCategory = "all";
                currentSearch = "";
                TextInputEditText etS = view.findViewById(R.id.et_search);
                if (etS != null) etS.setText("");
                productViewModel.setCategory("all");
                productViewModel.setSearch("");
                loadProducts(view);
            });
        }
    }
}
