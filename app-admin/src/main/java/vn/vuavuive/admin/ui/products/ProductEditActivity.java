package vn.vuavuive.admin.ui.products;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import dagger.hilt.android.AndroidEntryPoint;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.databinding.ActivityProductEditBinding;
import vn.vuavuive.shared.data.api.AdminProductApi;
import vn.vuavuive.shared.data.api.ProductApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.CategoryResponse;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.UploadResponse;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.SessionManager;

@AndroidEntryPoint
public class ProductEditActivity extends AppCompatActivity {
    private static final String TAG = "ProductEditActivity";

    @Inject AdminProductApi adminProductApi;
    @Inject ProductApi productApi;
    @Inject SessionManager sessionManager;

    private ActivityProductEditBinding binding;
    private User currentUser;
    private String productId;
    private Product existingProduct;
    private String selectedImageUrl;
    private Uri pendingImageUri;
    private boolean imageUploading = false;
    private final List<CategoryResponse> categories = new ArrayList<>();
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) return;
            pendingImageUri = uri;
            Glide.with(this).load(uri).placeholder(R.drawable.ic_image).error(R.drawable.ic_image)
                    .centerCrop().into(binding.ivProductPreview);
            uploadSelectedImage(uri);
        });
        binding = ActivityProductEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = sessionManager.getUser();
        if (currentUser != null && currentUser.getRole() != null) {
            currentUser.setRole(currentUser.getRole().toLowerCase());
        }
        if (currentUser == null) {
            finish();
            return;
        }

        productId = getIntent().getStringExtra("PRODUCT_ID");
        setupSpinner();
        loadCategories();
        setupListeners();
        if (productId != null && !productId.isEmpty()) {
            loadExistingProduct();
        } else {
            binding.tvTitle.setText("THEM SAN PHAM MOI");
            loadImagePreview(selectedImageUrl);
        }
        enforceRolePermissions();
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProductCategory.setAdapter(adapter);
    }

    private void loadCategories() {
        adminProductApi.getCategories().enqueue(new Callback<ApiResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CategoryResponse>>> call,
                                   Response<ApiResponse<List<CategoryResponse>>> response) {
                if (isFinishing() || isDestroyed()) return;
                ApiResponse<List<CategoryResponse>> api = response.body();
                List<CategoryResponse> body = api != null ? api.getData() : null;
                if (!response.isSuccessful() || body == null || body.isEmpty()) {
                    Log.w(TAG, "Category load failed HTTP " + response.code() + ": " + errorBody(response));
                    Toast.makeText(ProductEditActivity.this, "Khong tai duoc danh muc", Toast.LENGTH_SHORT).show();
                    return;
                }
                categories.clear();
                categories.addAll(body);

                List<String> names = new ArrayList<>();
                for (CategoryResponse category : categories) {
                    names.add(category.getName() != null ? category.getName() : category.getSlug());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ProductEditActivity.this, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerProductCategory.setAdapter(adapter);
                selectCategoryForProduct(existingProduct);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CategoryResponse>>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.w(TAG, "Category load error", t);
                Toast.makeText(ProductEditActivity.this, "Loi tai danh muc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnChooseImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void uploadSelectedImage(Uri uri) {
        try {
            imageUploading = true;
            binding.btnSaveProduct.setEnabled(false);
            binding.btnChooseImage.setEnabled(false);
            binding.btnChooseImage.setText("Dang tai anh...");

            String mime = getContentResolver().getType(uri);
            if (mime == null) mime = "image/jpeg";
            byte[] bytes = readBytes(uri);
            RequestBody body = RequestBody.create(bytes, MediaType.parse(mime));
            MultipartBody.Part file = MultipartBody.Part.createFormData("file", "product." + extension(mime), body);

            adminProductApi.uploadImage(file).enqueue(new Callback<ApiResponse<UploadResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<UploadResponse>> call,
                                       Response<ApiResponse<UploadResponse>> response) {
                    if (isFinishing() || isDestroyed()) return;
                    imageUploading = false;
                    binding.btnSaveProduct.setEnabled(true);
                    binding.btnChooseImage.setEnabled(true);
                    binding.btnChooseImage.setText("Chon anh tu Gallery");
                    ApiResponse<UploadResponse> body = response.body();
                    if (response.isSuccessful() && body != null && body.isSuccess()
                            && body.getData() != null && body.getData().getUrl() != null) {
                        selectedImageUrl = body.getData().getUrl();
                        Toast.makeText(ProductEditActivity.this, "Da tai anh", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Upload failed HTTP " + response.code() + ": " + errorBody(response));
                        Toast.makeText(ProductEditActivity.this, "Tai anh that bai", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UploadResponse>> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    imageUploading = false;
                    binding.btnSaveProduct.setEnabled(true);
                    binding.btnChooseImage.setEnabled(true);
                    binding.btnChooseImage.setText("Chon anh tu Gallery");
                    Log.w(TAG, "Upload error", t);
                    Toast.makeText(ProductEditActivity.this, "Loi tai anh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            imageUploading = false;
            binding.btnSaveProduct.setEnabled(true);
            binding.btnChooseImage.setEnabled(true);
            binding.btnChooseImage.setText("Chon anh tu Gallery");
            Log.w(TAG, "Cannot read image", e);
            Toast.makeText(this, "Khong doc duoc anh", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] readBytes(Uri uri) throws Exception {
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) throw new IllegalArgumentException("Cannot open image");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            return out.toByteArray();
        }
    }

    private String extension(String mime) {
        if ("image/png".equals(mime)) return "png";
        if ("image/webp".equals(mime)) return "webp";
        if ("image/gif".equals(mime)) return "gif";
        return "jpg";
    }

    private void loadExistingProduct() {
        binding.tvTitle.setText("CHINH SUA SAN PHAM");
        productApi.getProduct(productId).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (isFinishing() || isDestroyed()) return;
                ApiResponse<Product> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    existingProduct = body.getData();
                    bindProduct(existingProduct);
                    return;
                }
                Toast.makeText(ProductEditActivity.this, "Khong tim thay san pham", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(ProductEditActivity.this, "Loi ket noi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindProduct(Product product) {
        if (product == null) return;
        binding.etProductName.setText(product.getName());
        binding.etProductPrice.setText(String.format(Locale.US, "%.0f", product.getPrice()));
        if (product.getOriginalPrice() != null) {
            binding.etProductOriginalPrice.setText(String.format(Locale.US, "%.0f", product.getOriginalPrice()));
        }
        binding.etProductStock.setText(String.valueOf(product.getStock()));
        binding.etProductUnit.setText(product.getUnit());
        binding.etProductDescription.setText(product.getDescription());
        binding.switchIsActive.setChecked(product.isActive());
        if (product.getTags() != null) binding.etProductTags.setText(String.join(", ", product.getTags()));
        selectCategoryForProduct(product);
        selectedImageUrl = product.getImageUrl();
        loadImagePreview(selectedImageUrl);
    }

    private void selectCategoryForProduct(Product product) {
        if (product == null || categories.isEmpty() || product.getCategory() == null) return;
        for (int i = 0; i < categories.size(); i++) {
            CategoryResponse category = categories.get(i);
            if (product.getCategory().equalsIgnoreCase(category.getSlug())
                    || product.getCategory().equalsIgnoreCase(category.getName())) {
                binding.spinnerProductCategory.setSelection(i);
                return;
            }
        }
    }

    private void loadImagePreview(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .centerCrop()
                .into(binding.ivProductPreview);
    }

    private void enforceRolePermissions() {
        if (!"audit".equals(currentUser.getRole())) return;
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
        binding.btnSaveProduct.setText("READ ONLY");
        binding.btnSaveProduct.setOnClickListener(v ->
                Toast.makeText(this, "Tai khoan audit chi duoc xem", Toast.LENGTH_SHORT).show());
    }

    private void saveProduct() {
        if (!binding.btnSaveProduct.isEnabled()) return;
        if (imageUploading) {
            Toast.makeText(this, "Cho tai anh xong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pendingImageUri != null && (selectedImageUrl == null || selectedImageUrl.isEmpty())) {
            Toast.makeText(this, "Anh chua tai len thanh cong", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = binding.etProductName.getText().toString().trim();
        String priceStr = binding.etProductPrice.getText().toString().trim();
        String origPriceStr = binding.etProductOriginalPrice.getText().toString().trim();
        String stockStr = binding.etProductStock.getText().toString().trim();
        String unit = binding.etProductUnit.getText().toString().trim();
        String desc = binding.etProductDescription.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etProductName.setError("Nhap ten san pham");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            binding.etProductPrice.setError("Gia khong hop le");
            return;
        }

        double originalPrice = price;
        if (!origPriceStr.isEmpty()) {
            try {
                originalPrice = Double.parseDouble(origPriceStr);
                if (originalPrice < price) {
                    binding.etProductOriginalPrice.setError("Gia goc phai >= gia ban");
                    return;
                }
            } catch (NumberFormatException e) {
                binding.etProductOriginalPrice.setError("Gia goc khong hop le");
                return;
            }
        }

        int stock;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            binding.etProductStock.setError("Ton kho khong hop le");
            return;
        }
        if (unit.isEmpty()) {
            binding.etProductUnit.setError("Nhap don vi");
            return;
        }
        if (existingProduct != null && (existingProduct.getId() == null || existingProduct.getId().isEmpty())) {
            Toast.makeText(this, "San pham khong hop le", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryId = selectedCategoryId();
        if (categoryId == null) {
            Toast.makeText(this, "Chua co danh muc hop le", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", desc);
        body.put("originalPrice", originalPrice);
        body.put("sellingPrice", price);
        body.put("stockQuantity", stock);
        body.put("unit", unit);
        body.put("imageUrl", selectedImageUrl);
        body.put("categoryId", categoryId);
        body.put("isActive", binding.switchIsActive.isChecked());

        Call<Product> call = existingProduct != null
                ? adminProductApi.updateProduct(existingProduct.getId(), body)
                : adminProductApi.createProduct(body);
        binding.btnSaveProduct.setEnabled(false);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnSaveProduct.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductEditActivity.this, "Da luu san pham", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.w(TAG, "Save failed HTTP " + response.code() + ": " + errorBody(response));
                    Toast.makeText(ProductEditActivity.this, "Luu that bai", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnSaveProduct.setEnabled(true);
                Log.w(TAG, "Save error", t);
                Toast.makeText(ProductEditActivity.this, "Loi ket noi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String selectedCategoryId() {
        int position = binding.spinnerProductCategory.getSelectedItemPosition();
        if (position < 0 || position >= categories.size()) return null;
        return categories.get(position).getId();
    }

    private String errorBody(Response<?> response) {
        try {
            return response.errorBody() != null ? response.errorBody().string() : "";
        } catch (Exception ignored) {
            return "";
        }
    }
}
