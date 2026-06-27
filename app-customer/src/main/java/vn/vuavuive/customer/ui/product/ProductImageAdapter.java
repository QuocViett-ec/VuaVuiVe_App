package vn.vuavuive.customer.ui.product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import vn.vuavuive.customer.R;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.SliderViewHolder> {

    private final Context context;
    private final List<Object> images; // Can hold String URLs or Integer Resource IDs

    public ProductImageAdapter(Context context, List<Object> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_image_slide, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Object item = images.get(position);
        Glide.with(context)
                .load(item)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_slide);
        }
    }
}
