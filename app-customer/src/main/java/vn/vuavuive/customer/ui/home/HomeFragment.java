package vn.vuavuive.customer.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.product.ProductAdapter;
import vn.vuavuive.customer.ui.product.ProductDetailActivity;
import vn.vuavuive.customer.ui.recipe.RecipeAdapter;
import vn.vuavuive.customer.ui.recipe.RecipeDetailActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.CategoryViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.customer.viewmodel.RecipeViewModel;
import vn.vuavuive.shared.data.dto.CategoryResponse;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.local.CartItemEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private AuthViewModel authViewModel;
    private ProductViewModel productViewModel;
    private RecipeViewModel recipeViewModel;
    private CartViewModel cartViewModel;
    private CategoryViewModel categoryViewModel;

    private RecipeAdapter recipeAdapter;
    private ProductAdapter productAdapter;

    private ChipGroup cgRecipeCategories;
    private ChipGroup cgProductCategories;
    private LinearLayout llShortcutsFromDb;
    private NestedScrollView scrollView;
    private EditText etSearchHome;

    private String currentProductCategory = "all";
    private String currentProductSearch = "";
    private String currentRecipeCategory = "all";
    private boolean currentShortcutSaleOnly = false;

    // Holds all recipes from API for local category filtering
    private List<Map<String, Object>> allRecipes = new ArrayList<>();

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private boolean isProductLoading = false;

    private static final String PREFS_HOME = "vvv_home";
    private static final String KEY_PROMO_SHOWN = "promo_shown";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel    = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        recipeViewModel  = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
        cartViewModel    = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        categoryViewModel= new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        scrollView = view.findViewById(R.id.scroll_view);

        setupGreeting(view);
        setupAddressPicker(view);
        setupSearch(view);
        setupShortcuts(view);          // Flash Sale static + dynamic DB categories
        setupRecipeSection(view);
        setupProductSection(view);

        // Load data from real API
        loadRecipesFromApi();
        loadProducts(view);

        if (!hasShownPromoDialog()) {
            markPromoDialogShown();
            new Handler().postDelayed(this::showPromoPopup, 1000);
        }
    }

    // ── Greeting Setup ─────────────────────────────────────────────────────────
    private void setupGreeting(View view) {
        TextView tvGreeting = view.findViewById(R.id.tv_greeting_name);
        if (tvGreeting == null) return;
        try {
            authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                    tvGreeting.setText(user.getName().toUpperCase());
                } else {
                    tvGreeting.setText("VỰA VUI VẺ");
                }
            });
        } catch (Exception e) {
            tvGreeting.setText("VỰA VUI VẺ");
        }
    }

    // ── Address Picker Setup ───────────────────────────────────────────────────
    private void setupAddressPicker(View view) {
        TextView tvAddress = view.findViewById(R.id.tv_delivery_address);
        View btnChange = view.findViewById(R.id.btn_change_address);
        if (btnChange != null && tvAddress != null) {
            btnChange.setOnClickListener(v -> {
                EditText input = new EditText(requireContext());
                input.setText(tvAddress.getText().toString().replace("Giao đến: ", ""));
                input.setPadding(32, 16, 32, 16);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Nhập địa chỉ giao hàng mới")
                        .setView(input)
                        .setPositiveButton("Cập nhật", (dialog, which) -> {
                            String newAddress = input.getText().toString().trim();
                            if (!newAddress.isEmpty()) {
                                tvAddress.setText("Giao đến: " + newAddress);
                                Toast.makeText(getContext(), "Cập nhật địa chỉ thành công", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }
    }

    // ── Search Setup ───────────────────────────────────────────────────────────
    private void setupSearch(View view) {
        EditText etSearch = view.findViewById(R.id.et_search_home);
        etSearchHome = etSearch;
        View btnMenu = view.findViewById(R.id.btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Danh mục đang được phát triển thêm", Toast.LENGTH_SHORT).show());
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = () -> {
                        currentProductSearch = s.toString().trim();
                        productViewModel.setSearch(currentProductSearch);
                        loadProducts(view);
                        if (!currentProductSearch.isEmpty() && scrollView != null) {
                            View target = view.findViewById(R.id.cg_product_categories);
                            if (target != null) scrollView.smoothScrollTo(0, target.getTop() - 100);
                        }
                    };
                    searchHandler.postDelayed(searchRunnable, 400);
                }
            });
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (clearHomeSearch()) return;
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }

    private boolean clearHomeSearch() {
        if (etSearchHome == null || (!etSearchHome.hasFocus() && currentProductSearch.isEmpty())) return false;
        searchHandler.removeCallbacks(searchRunnable);
        if (etSearchHome.getText() != null && etSearchHome.getText().length() > 0) {
            etSearchHome.setText("");
            currentProductSearch = "";
            productViewModel.setSearch("");
            loadProducts(getView());
        }
        etSearchHome.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etSearchHome.getWindowToken(), 0);
        return true;
    }

    // ── Shortcuts Setup — Flash Sale (static) + DB categories (dynamic) ────────
    private void setupShortcuts(View view) {
        llShortcutsFromDb = view.findViewById(R.id.ll_shortcuts_from_db);

        // Flash Sale: always shown, navigates to dedicated Flash Sale page
        View flashSale = view.findViewById(R.id.sc_flash_sale);
        if (flashSale != null) {
            flashSale.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), vn.vuavuive.customer.ui.product.FlashSaleActivity.class);
                startActivity(intent);
            });
        }

        // Dynamic shortcuts from DB categories
        if (categoryViewModel != null) {
            categoryViewModel.getCategories().observe(getViewLifecycleOwner(), result -> {
                if (result != null
                        && result.status == AuthRepository.Result.Status.SUCCESS
                        && result.data != null
                        && !result.data.isEmpty()) {
                    buildDynamicShortcuts(view, result.data);
                }
                // On failure: no shortcuts added — Flash Sale alone is sufficient
            });
        }
    }

    /**
     * Dynamically creates one circular shortcut per backend category,
     * reusing the same visual style as the static Flash Sale shortcut.
     */
    private void buildDynamicShortcuts(View view, List<CategoryResponse> categories) {
        if (llShortcutsFromDb == null) return;
        llShortcutsFromDb.removeAllViews();

        for (CategoryResponse cat : categories) {
            String slug = cat.getSlug();
            String emoji = getCategoryEmoji(slug);
            String label = cat.getName();

            // Outer container (matches sc_flash_sale layout)
            LinearLayout container = new LinearLayout(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(80), LinearLayout.LayoutParams.WRAP_CONTENT);
            container.setLayoutParams(params);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(android.view.Gravity.CENTER);
            container.setClickable(true);
            container.setFocusable(true);

            // Circle background with emoji
            FrameLayout circle = new FrameLayout(requireContext());
            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(dpToPx(50), dpToPx(50));
            circle.setLayoutParams(circleParams);
            circle.setBackgroundResource(R.drawable.bg_avatar_circle);
            circle.setBackgroundTintList(requireContext().getColorStateList(R.color.primary_container));

            TextView tvEmoji = new TextView(requireContext());
            FrameLayout.LayoutParams emojiParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            emojiParams.gravity = android.view.Gravity.CENTER;
            tvEmoji.setLayoutParams(emojiParams);
            tvEmoji.setText(emoji);
            tvEmoji.setTextSize(24f);
            circle.addView(tvEmoji);

            // Label text
            TextView tvLabel = new TextView(requireContext());
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dpToPx(6);
            tvLabel.setLayoutParams(labelParams);
            tvLabel.setText(label);
            tvLabel.setTextColor(requireContext().getColor(R.color.text_primary));
            tvLabel.setTextSize(11f);
            tvLabel.setGravity(android.view.Gravity.CENTER);
            tvLabel.setMaxLines(2);
            tvLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);

            container.addView(circle);
            container.addView(tvLabel);

            // Click: select this category in chip group + scroll to products
            container.setOnClickListener(v -> {
                currentShortcutSaleOnly = false;
                currentProductCategory = slug;
                productViewModel.setCategory(slug);
                loadProducts(view);
                selectChipBySlug(slug);
                scrollToProducts(view);
            });

            llShortcutsFromDb.addView(container);
        }
    }

    /** Converts dp to pixels. */
    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /** Scrolls the page to the product section. */
    private void scrollToProducts(View view) {
        if (scrollView != null && view != null) {
            View target = view.findViewById(R.id.cg_product_categories);
            if (target != null) scrollView.smoothScrollTo(0, target.getTop() - 100);
        }
    }

    /** Checks the chip whose tag matches the given slug. */
    private void selectChipBySlug(String slug) {
        if (cgProductCategories == null) return;
        for (int i = 0; i < cgProductCategories.getChildCount(); i++) {
            View child = cgProductCategories.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                Object tag = chip.getTag();
                if (tag != null && tag.equals(slug)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    // ── Recipe Section Setup ───────────────────────────────────────────────────
    private void setupRecipeSection(View view) {
        cgRecipeCategories = view.findViewById(R.id.cg_recipe_categories);
        RecyclerView rvRecipes = view.findViewById(R.id.rv_recipes_home);

        if (rvRecipes != null) {
            recipeAdapter = new RecipeAdapter(requireContext(), recipe -> {
                Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
                intent.putExtra("recipe_id", (String) recipe.get("_id"));
                startActivity(intent);
            });
            recipeAdapter.setOnBuyIngredientsClickListener(this::buyRecipeIngredients);
            rvRecipes.setLayoutManager(
                    new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            rvRecipes.setAdapter(recipeAdapter);
        }

        // Recipe category chips are built dynamically after API responds
    }

    /** Loads real recipes from the API and rebuilds the recipe category chips. */
    private void loadRecipesFromApi() {
        recipeViewModel.loadRecipes();
        recipeViewModel.getRecipes().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes == null) return;
            allRecipes = recipes;
            buildRecipeCategoryChips();
            filterRecipes(currentRecipeCategory);
        });
    }

    /**
     * Builds recipe category chips dynamically from the actual recipe data
     * by extracting unique "category" values from the loaded recipe list.
     */
    private void buildRecipeCategoryChips() {
        if (cgRecipeCategories == null || getView() == null) return;
        cgRecipeCategories.removeAllViews();

        // "Tất cả" chip
        Chip allChip = new Chip(requireContext());
        allChip.setText("🥗 Tất cả");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setTag("all");
        styleChip(allChip);
        allChip.setOnClickListener(v -> {
            currentRecipeCategory = "all";
            filterRecipes("all");
        });
        cgRecipeCategories.addView(allChip);

        // Collect unique categories from live recipe data
        List<String> seenCategories = new ArrayList<>();
        for (Map<String, Object> recipe : allRecipes) {
            Object catObj = recipe.get("category");
            if (catObj instanceof String) {
                String cat = (String) catObj;
                if (!cat.isEmpty() && !seenCategories.contains(cat)) {
                    seenCategories.add(cat);
                }
            }
        }

        for (String cat : seenCategories) {
            Chip chip = new Chip(requireContext());
            chip.setText(getRecipeCategoryEmoji(cat) + " " + cat);
            chip.setCheckable(true);
            chip.setChecked(false);
            chip.setTag(cat);
            styleChip(chip);
            chip.setOnClickListener(v -> {
                currentRecipeCategory = cat;
                filterRecipes(cat);
            });
            cgRecipeCategories.addView(chip);
        }
    }

    private void filterRecipes(String category) {
        if (recipeAdapter == null) return;
        if ("all".equals(category)) {
            recipeAdapter.setRecipes(allRecipes);
        } else {
            List<Map<String, Object>> filtered = new ArrayList<>();
            for (Map<String, Object> r : allRecipes) {
                if (category.equals(r.get("category"))) filtered.add(r);
            }
            recipeAdapter.setRecipes(filtered);
        }
    }

    private String getRecipeCategoryEmoji(String cat) {
        if (cat == null) return "🍽️";
        switch (cat) {
            case "Món mặn":  return "🥩";
            case "Món canh": return "🍲";
            case "Xào, luộc": return "🥦";
            case "Món chay": return "🌿";
            case "Tráng miệng": return "🍮";
            default:         return "🍽️";
        }
    }

    @SuppressWarnings("unchecked")
    private void buyRecipeIngredients(Map<String, Object> recipe) {
        Object ingObj = recipe.get("ingredients");
        if (ingObj instanceof List) {
            for (Object ing : (List<?>) ingObj) {
                if (ing instanceof Map) {
                    Map<String, Object> ingredient = (Map<String, Object>) ing;
                    String name = ingredient.get("name") != null ? ingredient.get("name").toString() : "";
                    if (!name.isEmpty()) {
                        productViewModel.getProducts(null, name, 1, 1, null)
                                .observe(getViewLifecycleOwner(), result -> {
                                    if (result != null && result.data != null && !result.data.isEmpty()) {
                                        Product p = result.data.get(0);
                                        CartItemEntity item = new CartItemEntity();
                                        item.setProductId(p.getId());
                                        item.setProductName(p.getName());
                                        item.setProductPrice(p.getPrice());
                                        item.setProductImageUrl(p.getImageUrl());
                                        item.setProductUnit(p.getUnit());
                                        item.setProductStock(p.getStock());
                                        item.setQuantity(1);
                                        item.setAddedAt(System.currentTimeMillis());
                                        item.setSavedForLater(false);
                                        cartViewModel.addItem(item);
                                    }
                                });
                    }
                }
            }
            Toast.makeText(requireContext(),
                    "✅ Đang thêm nguyên liệu của \"" + recipe.get("name") + "\" vào giỏ hàng",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ── Product Section Setup ──────────────────────────────────────────────────
    private void setupProductSection(View view) {
        cgProductCategories = view.findViewById(R.id.cg_product_categories);
        RecyclerView rvProducts = view.findViewById(R.id.rv_products_home);

        // Load category chips from DB — no mock fallback
        loadCategoryChipsFromDb(view);

        if (rvProducts != null) {
            productAdapter = new ProductAdapter(requireContext(), product -> {
                Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            });
            productAdapter.setAddToCartListener(product -> {
                CartItemEntity item = new CartItemEntity();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setProductPrice(product.getPrice());
                item.setProductImageUrl(product.getImageUrl());
                item.setProductUnit(product.getUnit());
                item.setProductStock(product.getStock());
                item.setQuantity(1);
                item.setAddedAt(System.currentTimeMillis());
                item.setSavedForLater(false);
                cartViewModel.addItem(item);
                Toast.makeText(requireContext(),
                        "✅ Đã thêm \"" + product.getName() + "\" vào giỏ hàng", Toast.LENGTH_SHORT).show();
            });
            rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            rvProducts.setAdapter(productAdapter);
        }
    }

    /**
     * Fetches categories from the backend API and builds product filter chips.
     * No mock fallback — if the API fails the chip group stays empty
     * and all products continue to load via loadProducts().
     */
    private void loadCategoryChipsFromDb(View view) {
        if (categoryViewModel == null) return;
        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), result -> {
            if (result == null || result.status != AuthRepository.Result.Status.SUCCESS
                    || result.data == null || result.data.isEmpty()) {
                // API returned nothing — show "Tất cả" chip alone
                buildProductCategoryChips(view, new ArrayList<>());
                return;
            }
            buildProductCategoryChips(view, result.data);
        });
    }

    private void buildProductCategoryChips(View view, List<CategoryResponse> categories) {
        if (cgProductCategories == null) return;
        cgProductCategories.removeAllViews();

        // "Tất cả" first
        Chip allChip = new Chip(requireContext());
        allChip.setText("🛒 Tất cả");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setTag("all");
        styleChip(allChip);
        allChip.setOnClickListener(v -> {
            currentProductCategory = "all";
            currentShortcutSaleOnly = false;
            productViewModel.setCategory("all");
            loadProducts(view);
        });
        cgProductCategories.addView(allChip);

        for (CategoryResponse cat : categories) {
            String slug = cat.getSlug();
            Chip chip = new Chip(requireContext());
            chip.setText(getCategoryEmoji(slug) + " " + cat.getName());
            chip.setCheckable(true);
            chip.setChecked(false);
            chip.setTag(slug);
            styleChip(chip);
            chip.setOnClickListener(v -> {
                currentProductCategory = slug;
                currentShortcutSaleOnly = false;
                productViewModel.setCategory(slug);
                loadProducts(view);
            });
            cgProductCategories.addView(chip);
        }
    }

    /** Shared chip styling helper. */
    private void styleChip(Chip chip) {
        chip.setChipBackgroundColorResource(R.color.surface_variant);
        chip.setTextColor(getResources().getColorStateList(R.color.bottom_nav_color, null));
        chip.setChipStrokeColorResource(R.color.outline);
        chip.setChipStrokeWidth(1f);
    }

    /** Maps backend category slug → display emoji. */
    private String getCategoryEmoji(String slug) {
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

    // ── Load Products from API only ────────────────────────────────────────────
    private void loadProducts(View view) {
        try {
            isProductLoading = true;
            productViewModel.loadProducts(1).observe(getViewLifecycleOwner(), result -> {
                isProductLoading = false;
                if (result != null
                        && result.status == AuthRepository.Result.Status.SUCCESS
                        && result.data != null) {
                    List<Product> products = currentShortcutSaleOnly
                            ? filterSaleProducts(result.data)
                            : result.data;
                    if (productAdapter != null) productAdapter.setProducts(products);
                    updateEmptyState(view, products.isEmpty());
                } else {
                    updateEmptyState(view, true);
                }
            });
        } catch (Exception e) {
            isProductLoading = false;
        }
    }

    /** Filters products that are on sale (have a discount). */
    private List<Product> filterSaleProducts(List<Product> products) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p != null && (p.getDiscountPercent() > 0
                    || (p.getOriginalPrice() != null && p.getOriginalPrice() > p.getPrice()))) {
                result.add(p);
            }
        }
        return result;
    }

    private void updateEmptyState(View view, boolean isEmpty) {
        if (view == null) return;
        View emptyView = view.findViewById(R.id.layout_empty_home);
        if (emptyView != null) emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    // ── Promo popup ────────────────────────────────────────────────────────────
    private void showPromoPopup() {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("🎉 Vựa Vui Vẻ Khuyến Mãi 🎉")
                .setMessage("Nhập mã [VUAVUIVE] để được giảm giá 15% cho tất cả đơn hàng rau củ quả tươi sống hôm nay!\n\nĐặc biệt: FREESHIP cho tất cả các đơn hàng tươi sống.")
                .setPositiveButton("Sao chép mã", (dialog, which) ->
                        Toast.makeText(requireContext(), "Đã sao chép mã giảm giá: VUAVUIVE", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private boolean hasShownPromoDialog() {
        return requireContext()
                .getSharedPreferences(PREFS_HOME, Context.MODE_PRIVATE)
                .getBoolean(KEY_PROMO_SHOWN, false);
    }

    private void markPromoDialogShown() {
        requireContext()
                .getSharedPreferences(PREFS_HOME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_PROMO_SHOWN, true)
                .apply();
    }
}
