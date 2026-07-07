package vn.vuavuive.customer.ui.recipe;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private TextView tvPrepTime, tvCookTime, tvDifficulty;
    private RecyclerView rvIngredients;
    private IngredientAdapter ingredientAdapter;
    private LinearLayout llSteps;
    private RecipeIngredientCartHelper ingredientCartHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeViewModel  = new ViewModelProvider(this).get(RecipeViewModel.class);
        cartViewModel    = new ViewModelProvider(this).get(CartViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        ingredientCartHelper = new RecipeIngredientCartHelper(this, this, productViewModel, cartViewModel);

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

        recipeViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ivRecipe      = findViewById(R.id.iv_recipe);
        tvRecipeName  = findViewById(R.id.tv_recipe_name);
        tvDescription = findViewById(R.id.tv_description);
        tvPrepTime    = findViewById(R.id.tv_prep_time);
        tvCookTime    = findViewById(R.id.tv_cook_time);
        tvDifficulty  = findViewById(R.id.tv_difficulty);
        rvIngredients = findViewById(R.id.rv_ingredients);
        llSteps       = findViewById(R.id.ll_steps);

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
        
        tvPrepTime.setText(getString(recipe, "prepTime", "--"));
        tvCookTime.setText(getString(recipe, "cookTime", "--"));
        tvDifficulty.setText(getString(recipe, "difficulty", "--"));

        Glide.with(this)
                .load(recipe.get("image"))
                .placeholder(R.drawable.ic_image)
                .centerCrop()
                .into(ivRecipe);

        Object ingObj = recipe.get("ingredients");
        if (ingObj instanceof List) {
            ingredientAdapter.setIngredients((List<Map<String, Object>>) ingObj);
        }

        llSteps.removeAllViews();
        Object stepsObj = recipe.get("steps");
        if (stepsObj instanceof List) {
            List<String> steps = (List<String>) stepsObj;
            for (int i = 0; i < steps.size(); i++) {
                TextView tvStep = new TextView(this);
                tvStep.setText("Bước " + (i + 1) + ": " + steps.get(i));
                tvStep.setTextColor(getResources().getColor(R.color.text_primary));
                tvStep.setTextSize(15);
                tvStep.setPadding(0, 0, 0, 16);
                tvStep.setLineSpacing(3, 1.2f);
                llSteps.addView(tvStep);
            }
        }

        Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("recipeId", recipeId != null ? recipeId : "");
        meta.put("recipeName", getString(recipe, "name", ""));
        productViewModel.sendRecommendEvent(Constants.EVENT_VIEW_RECIPE, "", meta);
    }

    private void addIngredientToCart(Map<String, Object> ingredient) {
        ingredientCartHelper.addIngredient(ingredient);
    }

    @SuppressWarnings("unchecked")
    private void addAllToCart() {
        if (currentRecipe == null) return;
        Object ingObj = currentRecipe.get("ingredients");
        if (ingObj instanceof List) {
            for (Object ing : (List<?>) ingObj) {
                if (ing instanceof Map) ingredientCartHelper.addIngredient((Map<String, Object>) ing, false);
            }
        }
        Toast.makeText(this, "Đang thêm tất cả nguyên liệu vào giỏ...", Toast.LENGTH_SHORT).show();
    }

    private String getString(Map<String, Object> map, String key, String def) {
        Object val = map.get(key);
        return val != null ? val.toString() : def;
    }
}
