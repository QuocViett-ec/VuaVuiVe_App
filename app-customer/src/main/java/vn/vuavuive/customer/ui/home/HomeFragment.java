package vn.vuavuive.customer.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.ui.product.ProductAdapter;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.customer.viewmodel.AuthViewModel;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private ProductViewModel productViewModel;
    private AuthViewModel authViewModel;
    private ProductAdapter featuredAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupFeaturedProducts(view);
        loadData();
    }

    private void setupFeaturedProducts(View view) {
        RecyclerView rvFeatured = view.findViewById(R.id.rv_featured_products);
        featuredAdapter = new ProductAdapter(getContext(), product -> {
            // Navigate to product detail
            android.content.Intent intent = new android.content.Intent(
                    getContext(),
                    vn.vuavuive.customer.ui.product.ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        rvFeatured.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeatured.setAdapter(featuredAdapter);
    }

    private void loadData() {
        productViewModel.loadProducts(1).observe(getViewLifecycleOwner(), result -> {
            if (result.status == vn.vuavuive.customer.data.repository.AuthRepository.Result.Status.SUCCESS
                    && result.data != null) {
                // Show first 10 as featured
                int size = Math.min(result.data.size(), 10);
                featuredAdapter.setProducts(result.data.subList(0, size));
            }
        });
    }
}
