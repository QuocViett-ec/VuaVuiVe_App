package vn.vuavuive.customer.ui.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import vn.vuavuive.customer.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientVH> {

    public interface OnAddClickListener {
        void onAddClick(Map<String, Object> ingredient);
    }

    private final Context context;
    private final OnAddClickListener listener;
    private List<Map<String, Object>> ingredients = new ArrayList<>();

    public IngredientAdapter(Context context, OnAddClickListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    public void setIngredients(List<Map<String, Object>> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public IngredientVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new IngredientVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientVH holder, int position) {
        Map<String, Object> ing = ingredients.get(position);
        Object name = ing.get("name");
        Object qty  = ing.get("qty");
        Object unit = ing.get("unit");

        holder.tvName.setText(name != null ? name.toString() : "");
        String qtyUnit = (qty != null ? qty.toString() : "") + " " + (unit != null ? unit.toString() : "");
        holder.tvQty.setText(qtyUnit.trim());
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onAddClick(ing);
        });
    }

    @Override public int getItemCount() { return ingredients.size(); }

    static class IngredientVH extends RecyclerView.ViewHolder {
        TextView tvName, tvQty;
        ImageButton btnAdd;

        IngredientVH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_ingredient_name);
            tvQty  = v.findViewById(R.id.tv_qty);
            btnAdd = v.findViewById(R.id.btn_add);
        }
    }
}
