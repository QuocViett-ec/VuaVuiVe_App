package vn.vuavuive.customer.ui.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import vn.vuavuive.customer.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeVH> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Map<String, Object> recipe);
    }

    private final Context context;
    private final OnRecipeClickListener listener;
    private List<Map<String, Object>> allRecipes = new ArrayList<>();
    private List<Map<String, Object>> displayRecipes = new ArrayList<>();

    public RecipeAdapter(Context context, OnRecipeClickListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    public void setRecipes(List<Map<String, Object>> recipes) {
        this.allRecipes     = recipes;
        this.displayRecipes = new ArrayList<>(recipes);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            displayRecipes = new ArrayList<>(allRecipes);
        } else {
            displayRecipes = new ArrayList<>();
            String lower = query.toLowerCase();
            for (Map<String, Object> r : allRecipes) {
                Object name = r.get("name");
                if (name != null && name.toString().toLowerCase().contains(lower)) {
                    displayRecipes.add(r);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public RecipeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeVH holder, int position) {
        Map<String, Object> recipe = displayRecipes.get(position);
        Object name  = recipe.get("name");
        Object image = recipe.get("image");

        holder.tvName.setText(name != null ? name.toString() : "Công thức");

        Glide.with(context)
                .load(image)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClick(recipe);
        });
    }

    @Override public int getItemCount() { return displayRecipes.size(); }

    static class RecipeVH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;

        RecipeVH(View v) {
            super(v);
            ivImage = v.findViewById(R.id.iv_recipe);
            tvName  = v.findViewById(R.id.tv_recipe_name);
        }
    }
}
