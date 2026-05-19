package vn.vuavuive.customer.ui.recipe;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.customer.viewmodel.RecipeViewModel;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.Constants;
import java.util.List;
import java.util.Map;

@AndroidEntryPoint
public class RecipeDetailActivity extends AppCompatActivity {

    private RecipeViewModel recipeViewModel;
    private CartViewModel cartViewModel;
    private ProductViewModel productViewModel;
    private Map<String, Object> currentRecipe;
    private String recipeId;

    private ImageView ivRecipe;
    private TextView tvRecipeName, tvDescription;
    private RecyclerView rvIngredients;
    private IngredientAdapter ingredientAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeViewModel  = new ViewModelProvider(this).get(RecipeViewModel.class);
        cartViewModel    = new ViewModelProvider(this).get(CartViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        initViews();

        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId != null) {
            recipeViewModel.loadRecipeDetail(recipeId);
        }

        recipeViewModel.getCurrentRecipe().observe(this, recipe -> {
            if (recipe != null) {
                currentRecipe = recipe;
                bindRecipe(recipe);
            }
        });
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ivRecipe      = findViewById(R.id.iv_recipe);
        tvRecipeName  = findViewById(R.id.tv_recipe_name);
        tvDescription = findViewById(R.id.tv_description);
        rvIngredients = findViewById(R.id.rv_ingredients);

        ingredientAdapter = new IngredientAdapter(this, ingredient -> addIngredientToCart(ingredient));
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(ingredientAdapter);
        rvIngredients.setNestedScrollingEnabled(false);

        View btnAddAll = findViewById(R.id.btn_add_all);
        if (btnAddAll != null) {
            btnAddAll.setOnClickListener(v -> addAllToCart());
        }
    }

    @SuppressWarnings("unchecked")
    private void bindRecipe(Map<String, Object> recipe) {
        tvRecipeName.setText(getString(recipe, "name", "Công thức"));
        tvDescription.setText(getString(recipe, "description", ""));

        Glide.with(this)
                .load(recipe.get("image"))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(ivRecipe);

        Object ingObj = recipe.get("ingredients");
        if (ingObj instanceof List) {
            ingredientAdapter.setIngredients((List<Map<String, Object>>) ingObj);
        }

        Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("recipeId", recipeId != null ? recipeId : "");
        meta.put("recipeName", getString(recipe, "name", ""));
        productViewModel.sendRecommendEvent(Constants.EVENT_VIEW_RECIPE, "", meta);
    }

    private void addIngredientToCart(Map<String, Object> ingredient) {
        String name = getString(ingredient, "name", "");
        if (name.isEmpty()) return;

        productViewModel.getProducts(null, name, 1, 1, null).observe(this, result -> {
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
                Toast.makeText(this, "Đã thêm " + p.getName() + " vào giỏ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không tìm thấy sản phẩm: " + name, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void addAllToCart() {
        if (currentRecipe == null) return;
        Object ingObj = currentRecipe.get("ingredients");
        if (ingObj instanceof List) {
            for (Object ing : (List<?>) ingObj) {
                if (ing instanceof Map) addIngredientToCart((Map<String, Object>) ing);
            }
        }
        Toast.makeText(this, "Đang thêm tất cả nguyên liệu vào giỏ...", Toast.LENGTH_SHORT).show();
    }

    private String getString(Map<String, Object> map, String key, String def) {
        Object val = map.get(key);
        return val != null ? val.toString() : def;
    }
}
