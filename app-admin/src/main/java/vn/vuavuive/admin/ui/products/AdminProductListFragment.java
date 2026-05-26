package vn.vuavuive.admin.ui.products;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.FragmentAdminProductListBinding;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.User;

public class AdminProductListFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private FragmentAdminProductListBinding binding;
    private ProductAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();

    private String currentCategoryFilter = "all";
    private boolean currentLowStockFilter = false;
    private String currentSearchQuery = "";
    private User currentUser;

    private static final String[] SPINNER_DISPLAY_NAMES = {
            "T\u1ea5t c\u1ea3 danh m\u1ee5c",
            "Rau c\u1ee7 (veg)",
            "Tr\u00e1i c\u00e2y (fruit)",
            "Th\u1ecbt (meat)",
            "\u0110\u1ed3 u\u1ed1ng (drink)",
            "\u0110\u1ed3 kh\u00f4 (dry)",
            "Gia v\u1ecb (spice)",
            "\u0110\u1ed3 gia d\u1ee5ng (household)",
            "B\u00e1nh k\u1eb9o (sweet)",
            "\u0110\u00f4ng l\u1ea1nh (frozen)",
            "Kh\u00e1c (other)"
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
        super.onViewCreated(view, savedInstanceState);

        currentUser = MockRepository.getInstance().getCurrentUser();
        if (currentUser == null) return;

        setupSpinner();
        setupRecyclerView();
        setupFiltersAndFab();
        loadProducts();
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                SPINNER_DISPLAY_NAMES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilterCategory.setAdapter(spinnerAdapter);

        binding.spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategoryFilter = SPINNER_KEYS[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductAdapter(new ArrayList<>(), this);
        binding.rvProducts.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(this::loadProducts);
    }

    private void setupFiltersAndFab() {
        // Search filter
        binding.etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Low stock toggle chip
        binding.chipLowStock.setOnClickListener(v -> {
            currentLowStockFilter = !currentLowStockFilter;
            binding.chipLowStock.setChecked(currentLowStockFilter);
            applyFilters();
        });

        // FAB add trigger
        binding.fabAddProduct.setOnClickListener(v -> {
            if ("audit".equals(currentUser.getRole())) {
                Toast.makeText(getContext(),
                        "Ki\u1ec3m to\u00e1n vi\u00ean kh\u00f4ng c\u00f3 quy\u1ec1n t\u1ea1o s\u1ea3n ph\u1ea9m m\u1edbi",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), ProductEditActivity.class);
            startActivity(intent);
        });

        // CSV export
        binding.btnExportProductsCsv.setOnClickListener(v -> {
            if ("audit".equals(currentUser.getRole())) {
                Toast.makeText(getContext(),
                        "Ki\u1ec3m to\u00e1n vi\u00ean kh\u00f4ng c\u00f3 quy\u1ec1n xu\u1ea5t s\u1ea3n ph\u1ea9m",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            exportProductsCsv();
        });
    }

    private void loadProducts() {
        allProducts = new ArrayList<>(MockRepository.getInstance().getProducts());
        applyFilters();
        binding.swipeRefresh.setRefreshing(false);
    }

    private void applyFilters() {
        List<Product> filteredList = new ArrayList<>();
        for (Product p : allProducts) {
            boolean matchesCategory = "all".equals(currentCategoryFilter)
                    || currentCategoryFilter.equalsIgnoreCase(p.getCategory());
            boolean matchesStock = !currentLowStockFilter
                    || (p.getStock() <= 10 && p.isActive());

            boolean matchesQuery = false;
            if (currentSearchQuery.isEmpty()) {
                matchesQuery = true;
            } else {
                String q = currentSearchQuery.toLowerCase(Locale.getDefault());
                String name = p.getName() != null ? p.getName().toLowerCase() : "";
                String sub = p.getSubCategory() != null ? p.getSubCategory().toLowerCase() : "";

                boolean tagMatch = false;
                if (p.getTags() != null) {
                    for (String tag : p.getTags()) {
                        if (tag.toLowerCase().contains(q)) {
                            tagMatch = true;
                            break;
                        }
                    }
                }

                if (name.contains(q) || sub.contains(q) || tagMatch) {
                    matchesQuery = true;
                }
            }

            if (matchesCategory && matchesStock && matchesQuery) {
                filteredList.add(p);
            }
        }
        adapter.updateData(filteredList);
    }

    // Callbacks from ProductAdapter
    @Override
    public void onProductClick(Product product) {
        if ("audit".equals(currentUser.getRole())) {
            Toast.makeText(getContext(),
                    "Ki\u1ec3m to\u00e1n vi\u00ean ch\u1ec9 \u0111\u01b0\u1ee3c quy\u1ec1n xem (Read-Only)",
                    Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(getContext(), ProductEditActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }

    @Override
    public void onProductLongClick(Product product) {
        if ("audit".equals(currentUser.getRole())) {
            Toast.makeText(getContext(),
                    "Ki\u1ec3m to\u00e1n vi\u00ean kh\u00f4ng c\u00f3 quy\u1ec1n x\u00f3a s\u1ea3n ph\u1ea9m",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("X\u00e1c nh\u1eadn x\u00f3a s\u1ea3n ph\u1ea9m")
                .setMessage("B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n x\u00f3a s\u1ea3n ph\u1ea9m '"
                        + product.getName() + "' kh\u1ecfi h\u1ec7 th\u1ed1ng?")
                .setPositiveButton("X\u00f3a", (dialog, which) -> {
                    MockRepository.getInstance().deleteProduct(product.getId());
                    Toast.makeText(getContext(),
                            "\u0110\u00e3 x\u00f3a s\u1ea3n ph\u1ea9m th\u00e0nh c\u00f4ng",
                            Toast.LENGTH_SHORT).show();
                    loadProducts();
                })
                .setNegativeButton("H\u1ee7y", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
    }

    private void exportProductsCsv() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("M\u00e3 SP,T\u00ean S\u1ea3n Ph\u1ea9m,Danh M\u1ee5c,\u0110\u01a1n V\u1ecb,"
                    + "Gi\u00e1 B\u00e1n,Gi\u00e1 G\u1ed1c,T\u1ed3n Kho,\u0110\u00e3 B\u00e1n,K\u00edch Ho\u1ea1t\n");

            for (Product p : allProducts) {
                boolean matchesCategory = "all".equals(currentCategoryFilter)
                        || currentCategoryFilter.equalsIgnoreCase(p.getCategory());
                boolean matchesStock = !currentLowStockFilter
                        || (p.getStock() <= 10 && p.isActive());
                boolean matchesQuery = currentSearchQuery.isEmpty()
                        || (p.getName() != null
                                && p.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()));

                if (matchesCategory && matchesStock && matchesQuery) {
                    double original = p.getOriginalPrice() != null ? p.getOriginalPrice() : p.getPrice();
                    int sold = p.getSoldCount() != null ? p.getSoldCount() : 0;
                    csv.append(String.format("%s,%s,%s,%s,%.0f,%.0f,%d,%d,%b\n",
                            p.getId(),
                            p.getName().replace(",", " -"),
                            p.getCategory(),
                            p.getUnit() != null ? p.getUnit() : "N/A",
                            p.getPrice(),
                            original,
                            p.getStock(),
                            sold,
                            p.isActive()));
                }
            }

            String filename = "products_filtered_" + System.currentTimeMillis() + ".csv";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContext().getContentResolver()
                    .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream os = getContext().getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(csv.toString().getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        Toast.makeText(getContext(),
                                "\u0110\u00e3 l\u01b0u " + filename + " v\u00e0o Downloads!",
                                Toast.LENGTH_LONG).show();
                        MockRepository.getInstance().addAuditLog(
                                "Xu\u1ea5t b\u00e1o c\u00e1o s\u1ea3n ph\u1ea9m",
                                filename,
                                "Xu\u1ea5t th\u00e0nh c\u00f4ng t\u1eadp tin " + filename);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "L\u1ed7i khi l\u01b0u CSV: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
