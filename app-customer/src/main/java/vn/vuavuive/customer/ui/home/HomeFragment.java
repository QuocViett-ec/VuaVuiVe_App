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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.view.View;
import android.widget.ImageView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.product.ProductAdapter;
import vn.vuavuive.customer.ui.product.ProductDetailActivity;
import vn.vuavuive.customer.ui.recipe.RecipeAdapter;
import vn.vuavuive.customer.ui.recipe.RecipeDetailActivity;
import vn.vuavuive.customer.ui.recipe.RecipeIngredientCartHelper;
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
    private RecipeIngredientCartHelper ingredientCartHelper;

    private ChipGroup cgRecipeCategories;
    private LinearLayout llProductCategories;
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
    private LiveData<AuthRepository.Result<List<Product>>> productsLiveData;

    // Banner slider
    private final Handler bannerHandler = new Handler();
    private Runnable bannerRunnable;
    private static final long BANNER_INTERVAL_MS = 3000;

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
        ingredientCartHelper = new RecipeIngredientCartHelper(requireContext(), getViewLifecycleOwner(), productViewModel, cartViewModel);

        scrollView = view.findViewById(R.id.scroll_view);

        setupGreeting(view);
        setupAddressPicker(view);
        setupSearch(view);
        setupBannerSlider(view);
        setupRecipeSection(view);
        setupProductSection(view);
        setupVouchers(view);

        // Load data from real API
        loadRecipesFromApi();
        loadProducts(view);

        if (!hasShownPromoDialog()) {
            markPromoDialogShown();
            new Handler().postDelayed(this::showPromoPopup, 1000);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (authViewModel != null && authViewModel.isLoggedIn()) {
            authViewModel.checkSession().observe(getViewLifecycleOwner(), result -> {
                if (result != null && result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                    authViewModel.setCurrentUser(result.data);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bannerHandler.removeCallbacksAndMessages(null);
    }

    // ── Banner Slider Setup ─────────────────────────────────────────────────────
    private void setupBannerSlider(View view) {
        ViewPager2 viewPager = view.findViewById(R.id.vp_banner);
        LinearLayout dotsContainer = view.findViewById(R.id.ll_banner_dots);
        if (viewPager == null || dotsContainer == null) return;

        List<Integer> bannerRes = new ArrayList<>();
        bannerRes.add(R.drawable.banner_1);
        bannerRes.add(R.drawable.banner_2);
        bannerRes.add(R.drawable.banner_3);
        bannerRes.add(R.drawable.banner_4);

        BannerAdapter adapter = new BannerAdapter(bannerRes);
        viewPager.setAdapter(adapter);

        // Build dot indicators
        int dotSize = (int) (8 * getResources().getDisplayMetrics().density);
        int dotMargin = (int) (4 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < bannerRes.size(); i++) {
            ImageView dot = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotMargin, 0, dotMargin, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0
                    ? R.drawable.banner_dot_active
                    : R.drawable.banner_dot_inactive);
            dotsContainer.addView(dot);
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < dotsContainer.getChildCount(); i++) {
                    ImageView dot = (ImageView) dotsContainer.getChildAt(i);
                    dot.setBackgroundResource(i == position
                            ? R.drawable.banner_dot_active
                            : R.drawable.banner_dot_inactive);
                }
                // Reset auto-scroll timer
                bannerHandler.removeCallbacks(bannerRunnable);
                bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL_MS);
            }
        });

        bannerRunnable = () -> {
            int next = (viewPager.getCurrentItem() + 1) % bannerRes.size();
            viewPager.setCurrentItem(next, true);
        };
        bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL_MS);
    }

    // ── Greeting Setup ─────────────────────────────────────────────────────────
    private void setupGreeting(View view) {
        TextView tvGreeting = view.findViewById(R.id.tv_greeting_name);
        TextView tvPoints = view.findViewById(R.id.tv_member_points);
        if (tvGreeting == null) return;
        try {
            authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    if (user.getName() != null && !user.getName().isEmpty()) {
                        tvGreeting.setText(user.getName().toUpperCase());
                    } else {
                        tvGreeting.setText("VỰA VUI VẺ");
                    }
                    if (tvPoints != null) {
                        int points = user.getPoints();
                        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));
                        tvPoints.setText(nf.format(points) + " điểm");
                    }
                } else {
                    tvGreeting.setText("VỰA VUI VẺ");
                    if (tvPoints != null) {
                        tvPoints.setText("0 điểm");
                    }
                }
            });
        } catch (Exception e) {
            tvGreeting.setText("VỰA VUI VẺ");
            if (tvPoints != null) {
                tvPoints.setText("0 điểm");
            }
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
        View btnImageSearch = view.findViewById(R.id.btn_image_search_home);
        if (btnImageSearch != null) {
            btnImageSearch.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), vn.vuavuive.customer.ui.search.SearchActivity.class);
                intent.putExtra("open_image_picker", true);
                startActivity(intent);
            });
        }
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), vn.vuavuive.customer.ui.chat.ChatActivity.class);
                startActivity(intent);
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

    // ── Shortcuts Setup — Disabled (merged) ────────────────────────────────────
    private void setupShortcuts(View view) {
        // Disabled since we merged category selectors into the bottom bar
    }

    /** Converts dp to pixels. */
    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /** Scrolls the page to the product section. */
    private void scrollToProducts(View view) {
        if (scrollView != null && view != null) {
            View target = view.findViewById(R.id.hsv_product_categories);
            if (target != null) scrollView.smoothScrollTo(0, target.getTop() - 100);
        }
    }

    private void selectCategoryBySlug(String slug) {
        currentProductCategory = slug;
        updateCategorySelectionVisuals();
    }

    // ── Voucher Setup ──────────────────────────────────────────────────────────
    private void setupVouchers(View view) {
        LinearLayout container = view.findViewById(R.id.ll_vouchers_container);
        if (container == null) return;

        // Add copy listeners for default layout views first as fallback
        View btnCopy1 = view.findViewById(R.id.btn_copy_voucher_1);
        TextView tvCode1 = view.findViewById(R.id.tv_voucher_code_1);
        if (btnCopy1 != null && tvCode1 != null) {
            btnCopy1.setOnClickListener(v -> copyToClipboard(tvCode1.getText().toString()));
        }

        View btnCopy2 = view.findViewById(R.id.btn_copy_voucher_2);
        TextView tvCode2 = view.findViewById(R.id.tv_voucher_code_2);
        if (btnCopy2 != null && tvCode2 != null) {
            btnCopy2.setOnClickListener(v -> copyToClipboard(tvCode2.getText().toString()));
        }

        // Fetch vouchers dynamically from Firebase RTDB
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
            .child("vouchers")
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    if (getContext() == null || getView() == null) return;

                    List<vn.vuavuive.shared.data.dto.Voucher> activeVouchers = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (com.google.firebase.database.DataSnapshot s : snapshot.getChildren()) {
                            vn.vuavuive.shared.data.dto.Voucher v = mapSnapshotToVoucher(s);
                            if (v.isActive()) {
                                activeVouchers.add(v);
                            }
                        }
                    }

                    if (!activeVouchers.isEmpty()) {
                        container.removeAllViews();
                        for (vn.vuavuive.shared.data.dto.Voucher v : activeVouchers) {
                            View voucherCard = createVoucherCardView(container, v);
                            container.addView(voucherCard);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                    android.util.Log.e("HomeFragment", "Failed to load vouchers: " + error.getMessage());
                }
            });
    }

    private vn.vuavuive.shared.data.dto.Voucher mapSnapshotToVoucher(com.google.firebase.database.DataSnapshot s) {
        vn.vuavuive.shared.data.dto.Voucher v = new vn.vuavuive.shared.data.dto.Voucher();
        v.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
        v.setCode(s.child("code").getValue(String.class));
        v.setType(s.child("type").getValue(String.class));
        
        Double val = s.child("value").getValue(Double.class);
        v.setValue(val != null ? val : 0.0);
        
        Double minOrder = s.child("minOrderValue").getValue(Double.class);
        if (minOrder == null) minOrder = s.child("min_order_value").getValue(Double.class);
        v.setMinOrderValue(minOrder != null ? minOrder : 0.0);
        
        Boolean active = s.child("isActive").getValue(Boolean.class);
        if (active == null) active = s.child("is_active").getValue(Boolean.class);
        v.setActive(active != null ? active : true);
        
        v.setNote(s.child("note").getValue(String.class));
        return v;
    }

    private View createVoucherCardView(ViewGroup parent, vn.vuavuive.shared.data.dto.Voucher voucher) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_home_voucher, parent, false);
        TextView tvTitle = view.findViewById(R.id.tv_voucher_title);
        TextView tvSub = view.findViewById(R.id.tv_voucher_desc);
        TextView tvCode = view.findViewById(R.id.tv_voucher_code);
        View btnCopy = view.findViewById(R.id.btn_copy_voucher);

        String title = "";
        if ("PERCENTAGE".equalsIgnoreCase(voucher.getType()) || "percent".equalsIgnoreCase(voucher.getType())) {
            title = "GIẢM " + (int)voucher.getValue() + "%";
        } else if ("FIXED".equalsIgnoreCase(voucher.getType()) || "fixed".equalsIgnoreCase(voucher.getType())) {
            title = "GIẢM " + vn.vuavuive.shared.util.CurrencyFormatter.format(voucher.getValue());
        } else {
            title = "FREE SHIP";
        }
        tvTitle.setText(title);

        String desc = voucher.getNote() != null && !voucher.getNote().isEmpty()
                ? voucher.getNote()
                : "Cho đơn hàng từ " + vn.vuavuive.shared.util.CurrencyFormatter.format(voucher.getMinOrderValue());
        tvSub.setText(desc);

        tvCode.setText(voucher.getCode());

        btnCopy.setOnClickListener(v -> copyToClipboard(voucher.getCode()));
        return view;
    }

    private void copyToClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Voucher", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Đã sao chép mã: " + text, Toast.LENGTH_SHORT).show();
        }
    }

    // ── Recipe Section Setup ───────────────────────────────────────────────────
    private void setupRecipeSection(View view) {
        RecyclerView rvRecipes = view.findViewById(R.id.rv_recipes);

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
        
        TextView tvViewAllRecipes = view.findViewById(R.id.tv_view_all_recipes);
        if (tvViewAllRecipes != null) {
            tvViewAllRecipes.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), vn.vuavuive.customer.ui.recipe.RecipeListFragmentActivity.class);
                startActivity(intent);
            });
        }
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
                    ingredientCartHelper.addIngredient(ingredient, false);
                }
            }
            Toast.makeText(requireContext(),
                    "✅ Đang thêm nguyên liệu của \"" + recipe.get("name") + "\" vào giỏ hàng",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ── Product Section Setup ──────────────────────────────────────────────────
    private void setupProductSection(View view) {
        llProductCategories = view.findViewById(R.id.ll_product_categories);
        RecyclerView rvProducts = view.findViewById(R.id.rv_products_home);

        // Load category circles from DB
        loadCategoryCirclesFromDb(view);

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

    private void loadCategoryCirclesFromDb(View view) {
        if (categoryViewModel == null) return;
        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), result -> {
            if (result == null || result.status != AuthRepository.Result.Status.SUCCESS
                    || result.data == null || result.data.isEmpty()) {
                buildProductCategoryCircles(view, new ArrayList<>());
                return;
            }
            buildProductCategoryCircles(view, result.data);
        });
    }

    private void buildProductCategoryCircles(View view, List<CategoryResponse> categories) {
        if (llProductCategories == null) return;
        llProductCategories.removeAllViews();

        // 1. "Tất cả" circle first
        addCategoryCircleItem(view, "all", "🛒", "Tất cả", false);

        // 2. "Flash Sale" circle second
        addCategoryCircleItem(view, "flash_sale", "⚡", "Flash Sale", true);

        // 3. Dynamic categories from DB
        for (CategoryResponse cat : categories) {
            String slug = cat.getSlug();
            String emoji = getCategoryEmoji(slug);
            String label = cat.getName();
            addCategoryCircleItem(view, slug, emoji, label, false);
        }

        // Apply visual states
        updateCategorySelectionVisuals();
    }

    private void addCategoryCircleItem(View view, String slug, String emoji, String label, boolean isFlashSale) {
        LinearLayout container = new LinearLayout(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(76), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(2), 0, dpToPx(2), 0);
        container.setLayoutParams(params);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);
        container.setClickable(true);
        container.setFocusable(true);
        container.setTag(slug);

        FrameLayout circle = new FrameLayout(requireContext());
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(dpToPx(50), dpToPx(50));
        circle.setLayoutParams(circleParams);
        circle.setBackgroundResource(R.drawable.bg_avatar_circle);
        circle.setTag("circle_bg");

        TextView tvEmoji = new TextView(requireContext());
        FrameLayout.LayoutParams emojiParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        emojiParams.gravity = android.view.Gravity.CENTER;
        tvEmoji.setLayoutParams(emojiParams);
        tvEmoji.setText(emoji);
        tvEmoji.setTextSize(22f);
        circle.addView(tvEmoji);

        TextView tvLabel = new TextView(requireContext());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = dpToPx(6);
        tvLabel.setLayoutParams(labelParams);
        tvLabel.setText(label);
        tvLabel.setTextSize(11f);
        tvLabel.setGravity(android.view.Gravity.CENTER);
        tvLabel.setMaxLines(2);
        tvLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvLabel.setTag("label_text");

        container.addView(circle);
        container.addView(tvLabel);

        container.setOnClickListener(v -> {
            if (isFlashSale) {
                Intent intent = new Intent(requireContext(), vn.vuavuive.customer.ui.product.FlashSaleActivity.class);
                startActivity(intent);
            } else {
                currentProductCategory = slug;
                currentShortcutSaleOnly = false;
                productViewModel.setCategory(slug);
                loadProducts(view);
                updateCategorySelectionVisuals();
            }
        });

        llProductCategories.addView(container);
    }

    private void updateCategorySelectionVisuals() {
        if (llProductCategories == null) return;
        for (int i = 0; i < llProductCategories.getChildCount(); i++) {
            View child = llProductCategories.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout container = (LinearLayout) child;
                String slug = (String) container.getTag();

                FrameLayout circle = container.findViewWithTag("circle_bg");
                TextView label = container.findViewWithTag("label_text");

                if (circle == null || label == null) continue;

                boolean isSelected = slug != null && slug.equals(currentProductCategory);

                if (isSelected) {
                    circle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            requireContext().getColor(R.color.primary)));
                    label.setTextColor(requireContext().getColor(R.color.primary));
                    label.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                } else {
                    int tintColor = getCategoryPastelColor(slug);
                    circle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tintColor));
                    label.setTextColor(requireContext().getColor(R.color.text_secondary));
                    label.setTypeface(android.graphics.Typeface.DEFAULT);
                }
            }
        }
    }

    private int getCategoryPastelColor(String slug) {
        if (slug == null) return requireContext().getColor(R.color.surface_tinted);
        switch (slug) {
            case "all":
                return android.graphics.Color.parseColor("#E8F5ED");
            case "flash_sale":
                return android.graphics.Color.parseColor("#FFE0D6");
            case "veg":
                return android.graphics.Color.parseColor("#E8F5ED");
            case "fruit":
                return android.graphics.Color.parseColor("#FCEAEA");
            case "meat":
                return android.graphics.Color.parseColor("#FDF2E2");
            case "drink":
                return android.graphics.Color.parseColor("#E6F4FA");
            case "dry":
                return android.graphics.Color.parseColor("#FDF8E2");
            case "spice":
                return android.graphics.Color.parseColor("#FCEAEA");
            case "sweet":
                return android.graphics.Color.parseColor("#FCEAEF");
            case "frozen":
                return android.graphics.Color.parseColor("#E6F8FA");
            case "household":
                return android.graphics.Color.parseColor("#F0EFF4");
            default:
                return requireContext().getColor(R.color.surface_tinted);
        }
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
            if (productsLiveData != null) {
                productsLiveData.removeObservers(getViewLifecycleOwner());
            }
            productsLiveData = productViewModel.loadProducts(1);
            productsLiveData.observe(getViewLifecycleOwner(), result -> {
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

    private void styleChip(Chip chip) {
        chip.setChipBackgroundColorResource(R.color.surface_variant);
        chip.setTextColor(getResources().getColorStateList(R.color.bottom_nav_color, null));
        chip.setChipStrokeColorResource(R.color.outline);
        chip.setChipStrokeWidth(1f);
    }

    private void markPromoDialogShown() {
        requireContext()
                .getSharedPreferences(PREFS_HOME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_PROMO_SHOWN, true)
                .apply();
    }
}
