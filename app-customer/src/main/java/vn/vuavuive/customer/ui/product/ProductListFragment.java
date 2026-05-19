package vn.vuavuive.customer.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.search.SearchActivity;
import vn.vuavuive.customer.viewmodel.ProductViewModel;

@AndroidEntryPoint
public class ProductListFragment extends Fragment {

    private ProductViewModel productViewModel;
    private ProductAdapter adapter;
    private boolean isLoading = false;

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
        loadProducts();
    }

    private void setupSearch(View view) {
        TextInputEditText etSearch = view.findViewById(R.id.et_search);
        TextInputLayout tilSearch = view.findViewById(R.id.til_search);
        if (tilSearch != null) {
            tilSearch.setEndIconOnClickListener(v -> openSearch(getText(etSearch)));
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    productViewModel.setSearch(s.toString().trim());
                    loadProducts();
                };
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void openSearch(String prefillQuery) {
        Intent intent = new Intent(getContext(), SearchActivity.class);
        if (prefillQuery != null && !prefillQuery.isEmpty()) {
            intent.putExtra("prefill_query", prefillQuery);
        }
        startActivity(intent);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setupCategoryChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_categories);

        String[][] categories = {
                {"all", "Tất cả"}, {"veg", "Rau củ"}, {"fruit", "Trái cây"},
                {"meat", "Thịt"}, {"drink", "Đồ uống"}, {"dry", "Đồ khô"},
                {"spice", "Gia vị"}, {"sweet", "Bánh kẹo"}, {"frozen", "Đông lạnh"}
        };

        for (String[] cat : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat[1]);
            chip.setCheckable(true);
            chip.setChecked("all".equals(cat[0]));
            chip.setOnClickListener(v -> {
                productViewModel.setCategory(cat[0]);
                loadProducts();
            });
            chipGroup.addView(chip);
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_products);
        adapter = new ProductAdapter(getContext(), product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);

        // Endless scroll
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (!isLoading && !layoutManager.isSmoothScrolling()) {
                    int visibleCount = layoutManager.getChildCount();
                    int totalCount = layoutManager.getItemCount();
                    int firstVisible = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisible + visibleCount >= totalCount - 4) {
                        loadNextPage(view);
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh(View view) {
        SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadProducts();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void loadProducts() {
        showLoading(true);
        productViewModel.loadProducts(1).observe(getViewLifecycleOwner(), result -> {
            showLoading(false);
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                adapter.setProducts(result.data);
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                showEmptyState(requireView(), result.message);
            }
        });
    }

    private void loadNextPage(View view) {
        isLoading = true;
        productViewModel.loadNextPage().observe(getViewLifecycleOwner(), result -> {
            isLoading = false;
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                adapter.appendProducts(result.data);
            }
        });
    }

    private void showLoading(boolean show) {
        if (getView() == null) return;
        ProgressBar progress = requireView().findViewById(R.id.progress_bar);
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(View view, String message) {
        TextView tvEmpty = view.findViewById(R.id.tv_empty);
        if (tvEmpty != null) {
            tvEmpty.setText(message != null ? message : getString(R.string.label_empty));
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }
}
