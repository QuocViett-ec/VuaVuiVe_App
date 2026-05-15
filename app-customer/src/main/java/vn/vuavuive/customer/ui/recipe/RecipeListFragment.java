package vn.vuavuive.customer.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.viewmodel.RecipeViewModel;

@AndroidEntryPoint
public class RecipeListFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private RecipeAdapter recipeAdapter;
    private EditText etSearch;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);

        etSearch = view.findViewById(R.id.et_search);
        RecyclerView rvRecipes = view.findViewById(R.id.rv_recipes);

        recipeAdapter = new RecipeAdapter(requireContext(), recipe -> {
            Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
            intent.putExtra("recipe_id", (String) recipe.get("_id"));
            startActivity(intent);
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
