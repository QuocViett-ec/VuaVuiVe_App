package vn.vuavuive.admin.ui.products;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.ActivityProductEditBinding;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.User;

public class ProductEditActivity extends AppCompatActivity {

    private ActivityProductEditBinding binding;
    private MockRepository repo;
    private User currentUser;
    private String productId;
    private Product existingProduct;
    private String selectedImageUrl;

    private static final String[] CATEGORY_DISPLAY_NAMES = {
            "Rau củ (veg)", "Trái cây (fruit)", "Thịt (meat)", 
            "Đồ uống (drink)", "Đồ khô (dry)", "Gia vị (spice)", 
            "Đồ gia dụng (household)", "Bánh kẹo (sweet)", "Đông lạnh (frozen)", "Khác (other)"
    };

    private static final String[] CATEGORY_KEYS = {
            "veg", "fruit", "meat", "drink", "dry", "spice", "household", "sweet", "frozen", "other"
    };

    // A list of gorgeous mock images to cycle through when adding or picking a product image
    private static final String[] MOCK_IMAGES = {
            "https://images.unsplash.com/photo-1595855759920-86582396756a?auto=format&fit=crop&w=300&q=80",
            "https://images.unsplash.com/photo-1602470520998-f4a52199a3d6?auto=format&fit=crop&w=300&q=80",
            "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=300&q=80",
            "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=300&q=80",
            "https://images.unsplash.com/photo-1612927601601-6638404737ce?auto=format&fit=crop&w=300&q=80",
            "https://images.unsplash.com/photo-1508747703725-719777637510?auto=format&fit=crop&w=300&q=80"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repo = MockRepository.getInstance();
        currentUser = repo.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        productId = getIntent().getStringExtra("PRODUCT_ID");
        setupSpinner();
        setupListeners();

        if (productId != null) {
            loadExistingProduct();
        } else {
            // Setup default placeholder image for new products
            selectedImageUrl = MOCK_IMAGES[0];
            binding.tvTitle.setText("THÊM SẢN PHẨM MỚI");
            loadImagePreview(selectedImageUrl);
        }

        enforceRolePermissions();
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORY_DISPLAY_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProductCategory.setAdapter(adapter);
    }

    private void loadExistingProduct() {
        binding.tvTitle.setText("CHỈNH SỬA SẢN PHẨM");
        for (Product p : repo.getProducts()) {
            if (p.getId().equals(productId)) {
                existingProduct = p;
                break;
            }
        }

        if (existingProduct != null) {
            binding.etProductName.setText(existingProduct.getName());
            binding.etProductPrice.setText(String.format(Locale.US, "%.0f", existingProduct.getPrice()));
            if (existingProduct.getOriginalPrice() != null) {
                binding.etProductOriginalPrice.setText(String.format(Locale.US, "%.0f", existingProduct.getOriginalPrice()));
            }
            binding.etProductStock.setText(String.valueOf(existingProduct.getStock()));
            binding.etProductUnit.setText(existingProduct.getUnit());
            binding.etProductDescription.setText(existingProduct.getDescription());
            binding.switchIsActive.setChecked(existingProduct.isActive());

            // Set tags
            if (existingProduct.getTags() != null) {
                binding.etProductTags.setText(String.join(", ", existingProduct.getTags()));
            }

            // Set spinner selection
            for (int i = 0; i < CATEGORY_KEYS.length; i++) {
                if (CATEGORY_KEYS[i].equalsIgnoreCase(existingProduct.getCategory())) {
                    binding.spinnerProductCategory.setSelection(i);
                    break;
                }
            }

            selectedImageUrl = existingProduct.getImageUrl();
            loadImagePreview(selectedImageUrl);
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadImagePreview(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(binding.ivProductPreview);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnChooseImage.setOnClickListener(v -> {
            // Cyclically select a mock image to mimic choosing a premium photo
            int nextIndex = (int) (Math.random() * MOCK_IMAGES.length);
            selectedImageUrl = MOCK_IMAGES[nextIndex];
            loadImagePreview(selectedImageUrl);
            Toast.makeText(this, "Đã chọn ảnh mẫu chất lượng cao", Toast.LENGTH_SHORT).show();
        });

        binding.btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void enforceRolePermissions() {
        if ("audit".equals(currentUser.getRole())) {
            // Read-only access: disable inputs
            binding.etProductName.setEnabled(false);
            binding.etProductPrice.setEnabled(false);
            binding.etProductOriginalPrice.setEnabled(false);
            binding.etProductStock.setEnabled(false);
            binding.etProductUnit.setEnabled(false);
            binding.etProductDescription.setEnabled(false);
            binding.etProductTags.setEnabled(false);
            binding.spinnerProductCategory.setEnabled(false);
            binding.switchIsActive.setEnabled(false);
            binding.btnChooseImage.setVisibility(View.GONE);
            
            // Hide or style save button as read-only indicator
            binding.btnSaveProduct.setText("CHẾ ĐỘ XEM TIN (READ ONLY)");
            binding.btnSaveProduct.setBackgroundColor(0xFF555555);
            binding.btnSaveProduct.setOnClickListener(v -> {
                Toast.makeText(this, "Tài khoản kiểm toán chỉ có quyền xem thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveProduct() {
        String name = binding.etProductName.getText().toString().trim();
        String priceStr = binding.etProductPrice.getText().toString().trim();
        String origPriceStr = binding.etProductOriginalPrice.getText().toString().trim();
        String stockStr = binding.etProductStock.getText().toString().trim();
        String unit = binding.etProductUnit.getText().toString().trim();
        String desc = binding.etProductDescription.getText().toString().trim();
        String tagsStr = binding.etProductTags.getText().toString().trim();
        boolean isActive = binding.switchIsActive.isChecked();

        // 1. Validation
        if (name.isEmpty()) {
            binding.etProductName.setError("Vui lòng nhập tên sản phẩm");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                binding.etProductPrice.setError("Giá bán phải lớn hơn 0");
                return;
            }
        } catch (NumberFormatException e) {
            binding.etProductPrice.setError("Giá bán không hợp lệ");
            return;
        }

        Double originalPrice = null;
        if (!origPriceStr.isEmpty()) {
            try {
                originalPrice = Double.parseDouble(origPriceStr);
                if (originalPrice < price) {
                    binding.etProductOriginalPrice.setError("Giá gốc phải lớn hơn hoặc bằng giá bán");
                    return;
                }
            } catch (NumberFormatException e) {
                binding.etProductOriginalPrice.setError("Giá gốc không hợp lệ");
                return;
            }
        }

        int stock;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                binding.etProductStock.setError("Tồn kho không được âm");
                return;
            }
        } catch (NumberFormatException e) {
            binding.etProductStock.setError("Số lượng tồn kho không hợp lệ");
            return;
        }

        if (unit.isEmpty()) {
            binding.etProductUnit.setError("Vui lòng nhập đơn vị sản phẩm");
            return;
        }

        // 2. Prepare categories
        int selectedCatPos = binding.spinnerProductCategory.getSelectedItemPosition();
        String category = CATEGORY_KEYS[selectedCatPos];
        String subCategory = CATEGORY_DISPLAY_NAMES[selectedCatPos].split(" ")[0]; // Take display prefix

        // 3. Assemble tags
        List<String> tags = new ArrayList<>();
        if (!tagsStr.isEmpty()) {
            String[] split = tagsStr.split(",");
            for (String tag : split) {
                if (!tag.trim().isEmpty()) {
                    tags.add(tag.trim().toLowerCase());
                }
            }
        }

        // 4. Save
        Product p = existingProduct != null ? existingProduct : new Product();
        p.setName(name);
        p.setSlug(name.toLowerCase().replace(" ", "-"));
        p.setPrice(price);
        p.setOriginalPrice(originalPrice);
        p.setStock(stock);
        p.setUnit(unit);
        p.setCategory(category);
        p.setSubCategory(subCategory);
        p.setDescription(desc);
        p.setTags(tags);
        p.setActive(isActive);
        p.setImageUrl(selectedImageUrl);

        if (existingProduct != null) {
            repo.updateProduct(p);
            Toast.makeText(this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
        } else {
            p.setRating(5.0);
            p.setReviewCount(0);
            p.setSoldCount(0);
            repo.addProduct(p);
            Toast.makeText(this, "Đã thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
