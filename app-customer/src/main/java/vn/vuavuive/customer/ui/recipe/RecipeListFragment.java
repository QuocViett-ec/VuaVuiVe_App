package vn.vuavuive.customer.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.customer.viewmodel.RecipeViewModel;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.local.CartItemEntity;
import java.util.List;
import java.util.Map;

@AndroidEntryPoint
public class RecipeListFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private CartViewModel cartViewModel;
    private ProductViewModel productViewModel;
    private RecipeAdapter recipeAdapter;
    private EditText etSearch;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        etSearch = view.findViewById(R.id.et_search);
        RecyclerView rvRecipes = view.findViewById(R.id.rv_recipes);

        recipeAdapter = new RecipeAdapter(requireContext(), recipe -> {
            Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
            intent.putExtra("recipe_id", (String) recipe.get("_id"));
            startActivity(intent);
        });

        recipeAdapter.setOnBuyIngredientsClickListener(recipe -> {
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
                Toast.makeText(requireContext(), "✅ Đang thêm nguyên liệu của \"" + recipe.get("name") + "\" vào giỏ", Toast.LENGTH_SHORT).show();
            }
        });

        rvRecipes.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvRecipes.setAdapter(recipeAdapter);

        recipeViewModel.getRecipes().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes != null) recipeAdapter.setRecipes(recipes);
        });
        recipeViewModel.loadRecipes();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                recipeAdapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
