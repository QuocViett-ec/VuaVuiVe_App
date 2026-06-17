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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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
import vn.vuavuive.customer.data.MockDataProvider;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.product.ProductAdapter;
import vn.vuavuive.customer.ui.product.ProductDetailActivity;
import vn.vuavuive.customer.ui.recipe.RecipeAdapter;
import vn.vuavuive.customer.ui.recipe.RecipeDetailActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.customer.viewmodel.RecipeViewModel;
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

    private RecipeAdapter recipeAdapter;
    private ProductAdapter productAdapter;

    private ChipGroup cgRecipeCategories;
    private ChipGroup cgProductCategories;
    private NestedScrollView scrollView;

    private String currentProductCategory = "all";
    private String currentProductSearch = "";
    private String currentRecipeCategory = "all";
    private String[] currentShortcutKeywords = null;
    private boolean currentShortcutSaleOnly = false;

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

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        scrollView = view.findViewById(R.id.scroll_view);

        setupGreeting(view);
        setupAddressPicker(view);
        setupSearch(view);
        setupShortcuts(view);
        setupRecipeSection(view);
        setupProductSection(view);

        // Load initially
        loadRecipes();
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
        View btnMenu = view.findViewById(R.id.btn_menu);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Danh mục đang được phát triển thêm", Toast.LENGTH_SHORT).show();
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
                        currentProductSearch = s.toString().trim();
                        productViewModel.setSearch(currentProductSearch);
                        loadProducts(view);

                        // Smooth scroll to product section to show results
                        if (!currentProductSearch.isEmpty() && scrollView != null) {
                            View target = view.findViewById(R.id.cg_product_categories);
                            if (target != null) {
                                scrollView.smoothScrollTo(0, target.getTop() - 100);
                            }
                        }
                    };
                    searchHandler.postDelayed(searchRunnable, 400);
                }
            });
        }
    }

    // ── Shortcuts Setup ────────────────────────────────────────────────────────
    private void setupShortcuts(View view) {
        View flashSale = view.findViewById(R.id.sc_flash_sale);
        View banhTuoi = view.findViewById(R.id.sc_banh_tuoi);
        View miAnLien = view.findViewById(R.id.sc_mi_an_lien);
        View bia = view.findViewById(R.id.sc_bia);
        View suaTuoi = view.findViewById(R.id.sc_sua_tuoi);

        if (flashSale != null) flashSale.setOnClickListener(v -> selectShortcut(true));
        if (banhTuoi != null) banhTuoi.setOnClickListener(v -> selectShortcut("bánh", "banh", "cake", "bread", "sweet"));
        if (miAnLien != null) miAnLien.setOnClickListener(v -> selectShortcut("mì", "mi", "noodle", "dry"));
        if (bia != null) bia.setOnClickListener(v -> selectShortcut("bia", "beer", "drink"));
        if (suaTuoi != null) suaTuoi.setOnClickListener(v -> selectShortcut("sữa", "sua", "milk", "drink"));
    }

    private void selectShortcut(String... keywords) {
        currentShortcutSaleOnly = false;
        currentShortcutKeywords = keywords;
        selectProductCategory("all");
    }

    private void selectShortcut(boolean saleOnly) {
        currentShortcutSaleOnly = saleOnly;
        currentShortcutKeywords = null;
        selectProductCategory("all");
    }

    private void selectProductCategory(String catId) {
        if (cgProductCategories == null) return;
        currentProductCategory = catId;
        productViewModel.setCategory(catId);
        loadProducts(getView());

        // Select chip programmatically
        for (int i = 0; i < cgProductCategories.getChildCount(); i++) {
            View child = cgProductCategories.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                Object tag = chip.getTag();
                if (tag != null && tag.equals(catId)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        // Smooth scroll to product grid
        if (scrollView != null && getView() != null) {
            View target = getView().findViewById(R.id.cg_product_categories);
            if (target != null) {
                scrollView.smoothScrollTo(0, target.getTop() - 100);
            }
        }
    }

    // ── Recipes Setup ──────────────────────────────────────────────────────────
    private void setupRecipeSection(View view) {
        cgRecipeCategories = view.findViewById(R.id.cg_recipe_categories);
        RecyclerView rvRecipes = view.findViewById(R.id.rv_recipes_home);

        if (cgRecipeCategories != null) {
            String[][] recipeCats = {
                    {"all", "🥗 Tất cả"},
                    {"Món mặn", "🥩 Món mặn"},
                    {"Xào, luộc", "🥦 Xào, luộc -46%"},
                    {"Món canh", "🍲 Món canh -39%"}
            };

            for (String[] cat : recipeCats) {
                Chip chip = new Chip(requireContext());
                chip.setText(cat[1]);
                chip.setCheckable(true);
                chip.setChecked("all".equals(cat[0]));
                chip.setTag(cat[0]);
                chip.setChipBackgroundColorResource(R.color.surface_variant);
                chip.setTextColor(getResources().getColorStateList(R.color.bottom_nav_color, null));
                chip.setChipStrokeColorResource(R.color.outline);
                chip.setChipStrokeWidth(1f);
                chip.setOnClickListener(v -> {
                    currentRecipeCategory = cat[0];
                    filterRecipes(currentRecipeCategory);
                });
                cgRecipeCategories.addView(chip);
            }
        }

        if (rvRecipes != null) {
            recipeAdapter = new RecipeAdapter(requireContext(), recipe -> {
                Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
                intent.putExtra("recipe_id", (String) recipe.get("_id"));
                startActivity(intent);
            });

            recipeAdapter.setOnBuyIngredientsClickListener(this::buyRecipeIngredients);

            rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            rvRecipes.setAdapter(recipeAdapter);
        }
    }

    private void loadRecipes() {
        if (recipeAdapter != null) {
            filterRecipes(currentRecipeCategory);
        }
    }

    private void filterRecipes(String category) {
        if (recipeAdapter == null) return;
        List<Map<String, Object>> all = MockDataProvider.getMockRecipes();

        if ("all".equals(category)) {
            recipeAdapter.setRecipes(all);
        } else {
            List<Map<String, Object>> filtered = new ArrayList<>();
            for (Map<String, Object> r : all) {
                if (category.equals(r.get("category"))) {
                    filtered.add(r);
                }
            }
            recipeAdapter.setRecipes(filtered);
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
                        productViewModel.getProducts(null, name, 1, 1, null).observe(getViewLifecycleOwner(), result -> {
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
            Toast.makeText(requireContext(), "✅ Đang thêm nguyên liệu của \"" + recipe.get("name") + "\" vào giỏ hàng", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Product Section Setup ──────────────────────────────────────────────────
    private void setupProductSection(View view) {
        cgProductCategories = view.findViewById(R.id.cg_product_categories);
        RecyclerView rvProducts = view.findViewById(R.id.rv_products_home);

        if (cgProductCategories != null) {
            for (String[] cat : MockDataProvider.CATEGORIES) {
                Chip chip = new Chip(requireContext());
                chip.setText(cat[1]);
                chip.setCheckable(true);
                chip.setChecked("all".equals(cat[0]));
                chip.setTag(cat[0]);
                chip.setChipBackgroundColorResource(R.color.surface_variant);
                chip.setTextColor(getResources().getColorStateList(R.color.bottom_nav_color, null));
                chip.setChipStrokeColorResource(R.color.outline);
                chip.setChipStrokeWidth(1f);
                chip.setOnClickListener(v -> {
                    currentProductCategory = cat[0];
                    currentShortcutKeywords = null;
                    currentShortcutSaleOnly = false;
                    productViewModel.setCategory(cat[0]);
                    loadProducts(view);
                });
                cgProductCategories.addView(chip);
            }
        }

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
                Toast.makeText(requireContext(), "✅ Đã thêm \"" + product.getName() + "\" vào giỏ hàng", Toast.LENGTH_SHORT).show();
            });

            rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            rvProducts.setAdapter(productAdapter);
        }
    }

    private void loadProducts(View view) {
        // Mock data immediately for instant loading state
        List<Product> mockProducts = currentProductSearch.isEmpty()
                ? MockDataProvider.getMockProductsByCategory(currentProductCategory)
                : MockDataProvider.searchMockProducts(currentProductSearch);
        mockProducts = applyShortcutFilter(mockProducts);

        if (productAdapter != null) {
            productAdapter.setProducts(mockProducts);
        }
        updateEmptyState(view, mockProducts.isEmpty());

        try {
            isProductLoading = true;
            productViewModel.loadProducts(1).observe(getViewLifecycleOwner(), result -> {
                isProductLoading = false;
                if (result != null
                        && result.status == AuthRepository.Result.Status.SUCCESS
                        && result.data != null && !result.data.isEmpty()) {
                    List<Product> products = applyShortcutFilter(result.data);
                    if (productAdapter != null) {
                        productAdapter.setProducts(products);
                    }
                    updateEmptyState(view, products.isEmpty());
                }
            });
        } catch (Exception e) {
            isProductLoading = false;
        }
    }

    private void updateEmptyState(View view, boolean isEmpty) {
        if (view == null) return;
        View emptyView = view.findViewById(R.id.layout_empty_home);
        if (emptyView != null) {
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    // ── Promotion Popup ────────────────────────────────────────────────────────
    private List<Product> applyShortcutFilter(List<Product> products) {
        if (products == null || products.isEmpty() || (!currentShortcutSaleOnly && currentShortcutKeywords == null)) {
            return products;
        }
        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (currentShortcutSaleOnly ? isSaleProduct(product) : matchesKeywords(product, currentShortcutKeywords)) {
                filtered.add(product);
            }
        }
        return filtered;
    }

    private boolean isSaleProduct(Product product) {
        return product != null && (product.getDiscountPercent() > 0
                || (product.getOriginalPrice() != null && product.getOriginalPrice() > product.getPrice()));
    }

    private boolean matchesKeywords(Product product, String[] keywords) {
        if (product == null || keywords == null) return false;
        StringBuilder text = new StringBuilder();
        append(text, product.getName());
        append(text, product.getCategory());
        append(text, product.getSubCategory());
        if (product.getTags() != null) {
            for (String tag : product.getTags()) append(text, tag);
        }
        String haystack = text.toString().toLowerCase(java.util.Locale.ROOT);
        for (String keyword : keywords) {
            if (haystack.contains(keyword.toLowerCase(java.util.Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void append(StringBuilder builder, String value) {
        if (value != null) builder.append(' ').append(value);
    }

    private void showPromoPopup() {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("🎉 Vựa Vui Vẻ Khuyến Mãi 🎉")
                .setMessage("Nhập mã [VUAVUIVE] để được giảm giá 15% cho tất cả đơn hàng rau củ quả tươi sống hôm nay!\n\nĐặc biệt: FREESHIP cho tất cả các đơn hàng tươi sống.")
                .setPositiveButton("Sao chép mã", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Đã sao chép mã giảm giá: VUAVUIVE", Toast.LENGTH_SHORT).show();
                })
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
