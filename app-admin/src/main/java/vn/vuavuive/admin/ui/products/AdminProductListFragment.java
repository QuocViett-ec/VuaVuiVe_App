package vn.vuavuive.admin.ui.products;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import dagger.hilt.android.AndroidEntryPoint;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.FragmentAdminProductListBinding;
import vn.vuavuive.shared.data.api.AdminProductApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.SessionManager;

@AndroidEntryPoint
public class AdminProductListFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    @Inject AdminProductApi adminProductApi;
    @Inject SessionManager sessionManager;

    private FragmentAdminProductListBinding binding;
    private ProductAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();
    private String currentCategoryFilter = "all";
    private boolean currentLowStockFilter = false;
    private String currentSearchQuery = "";
    private User currentUser;

    private static final String[] SPINNER_DISPLAY_NAMES = {
            "Tat ca", "Rau cu", "Trai cay", "Thit", "Do uong", "Do kho",
            "Gia vi", "Do gia dung", "Banh keo", "Dong lanh", "Khac"
    };
    private static final String[] SPINNER_KEYS = {
            "all", "veg", "fruit", "meat", "drink", "dry", "spice", "household", "sweet", "frozen", "other"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminProductListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        currentUser = sessionManager.getUser();
        if (currentUser == null) return;
        if (currentUser.getRole() != null) currentUser.setRole(currentUser.getRole().toLowerCase());
        setupRecyclerView();
        setupSpinner();
        setupFiltersAndFab();
        loadProducts();
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, SPINNER_DISPLAY_NAMES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilterCategory.setAdapter(spinnerAdapter);
        binding.spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategoryFilter = SPINNER_KEYS[position];
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductAdapter(new ArrayList<>(), this);
        binding.rvProducts.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(this::loadProducts);
    }

    private void setupFiltersAndFab() {
        binding.etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }
        });
        binding.chipLowStock.setOnClickListener(v -> {
            currentLowStockFilter = !currentLowStockFilter;
            binding.chipLowStock.setChecked(currentLowStockFilter);
            applyFilters();
        });
        binding.fabAddProduct.setOnClickListener(v -> {
            if (isAudit()) {
                Toast.makeText(getContext(), "Read-only account", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(getContext(), ProductEditActivity.class));
        });
        binding.btnExportProductsCsv.setOnClickListener(v -> exportProductsCsv());
    }

    private void loadProducts() {
        binding.swipeRefresh.setRefreshing(true);
        // Call real API
        adminProductApi.getAllProducts(1, 100, "", currentCategoryFilter).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    allProducts = new ArrayList<>(response.body().getData());
                    applyFilters();
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách sản phẩm!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        if (binding == null || adapter == null || allProducts == null) return;
        List<Product> filteredList = new ArrayList<>();
        String q = currentSearchQuery.toLowerCase(Locale.getDefault());
        for (Product p : allProducts) {
            String category = p.getCategory() != null ? p.getCategory() : "";
            boolean matchesCategory = "all".equals(currentCategoryFilter)
                    || currentCategoryFilter.equalsIgnoreCase(category);
            boolean matchesStock = !currentLowStockFilter || (p.getStock() <= 10 && p.isActive());
            boolean matchesQuery = q.isEmpty()
                    || (p.getName() != null && p.getName().toLowerCase(Locale.getDefault()).contains(q))
                    || (p.getSubCategory() != null && p.getSubCategory().toLowerCase(Locale.getDefault()).contains(q));
            if (matchesCategory && matchesStock && matchesQuery) filteredList.add(p);
        }
        adapter.updateData(filteredList);
    }

    @Override
    public void onProductClick(Product product) {
        if (product == null || product.getId() == null || product.getId().isEmpty()) {
            Toast.makeText(getContext(), "San pham khong hop le", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getContext(), ProductEditActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }

    @Override
    public void onProductLongClick(Product product) {
        if (isAudit()) {
            Toast.makeText(getContext(), "Read-only account", Toast.LENGTH_SHORT).show();
            return;
        }
        if (product == null || product.getId() == null || product.getId().isEmpty()) {
            Toast.makeText(getContext(), "San pham khong hop le", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle("Xoa san pham")
                .setMessage(product.getName())
                .setPositiveButton("Xoa", (dialog, which) ->
                        adminProductApi.deleteProduct(product.getId()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call,
                                                   @NonNull Response<Void> response) {
                                if (binding == null || !isAdded()) return;
                                Toast.makeText(getContext(), "Da xoa san pham", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            }
                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                if (binding == null || !isAdded()) return;
                                Toast.makeText(getContext(), "Loi xoa: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Huy", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && adapter != null) loadProducts();
    }

    private boolean isAudit() {
        return currentUser != null && "audit".equals(currentUser.getRole());
    }

    private void exportProductsCsv() {
        try {
            StringBuilder csv = new StringBuilder("Ma SP,Ten,Danh muc,Don vi,Gia,Ton kho,Kich hoat\n");
            for (Product p : allProducts) {
                csv.append(String.format(Locale.US, "%s,%s,%s,%s,%.0f,%d,%b\n",
                        p.getId(),
                        safeCsv(p.getName()),
                        safeCsv(p.getCategory()),
                        safeCsv(p.getUnit()),
                        p.getPrice(),
                        p.getStock(),
                        p.isActive()));
            }
            ContentValues values = new ContentValues();
            String filename = "products_" + System.currentTimeMillis() + ".csv";
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return;
            try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                if (os != null) os.write(csv.toString().getBytes(StandardCharsets.UTF_8));
            }
            Toast.makeText(getContext(), "Da luu " + filename, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Loi CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String safeCsv(String value) {
        return value == null ? "" : value.replace(",", " -");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
