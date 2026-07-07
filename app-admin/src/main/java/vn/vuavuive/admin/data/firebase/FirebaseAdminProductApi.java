package vn.vuavuive.admin.data.firebase;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.vuavuive.shared.data.api.AdminProductApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.CategoryResponse;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.UploadResponse;

public class FirebaseAdminProductApi implements AdminProductApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private Retrofit retrofit;

    public FirebaseAdminProductApi() {}

    public FirebaseAdminProductApi(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    private Product mapSnapshotToProduct(DataSnapshot s) {
        Product p = new Product();
        p.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
        p.setName(s.child("name").getValue(String.class));
        p.setSlug(s.child("slug").getValue(String.class));
        
        Double sellingPrice = s.child("selling_price").getValue(Double.class);
        p.setPrice(sellingPrice != null ? sellingPrice : 0.0);
        
        p.setOriginalPrice(s.child("original_price").getValue(Double.class));
        p.setCategory(s.child("category_id").getValue(String.class));
        p.setSubCategory(s.child("sub_category").getValue(String.class));
        p.setDescription(s.child("description").getValue(String.class));
        
        String imgUrl = s.child("image_url").getValue(String.class);
        if (imgUrl == null) imgUrl = s.child("imageUrl").getValue(String.class);
        p.setImageUrl(imgUrl);
        
        Integer stock = s.child("stock_quantity").getValue(Integer.class);
        if (stock == null) stock = s.child("stock").getValue(Integer.class);
        p.setStock(stock != null ? stock : 0);
        
        p.setUnit(s.child("unit").getValue(String.class));
        
        List<String> tags = new ArrayList<>();
        DataSnapshot tagsSnap = s.child("tags");
        if (tagsSnap.exists()) {
            for (DataSnapshot t : tagsSnap.getChildren()) {
                tags.add(t.getValue(String.class));
            }
        }
        p.setTags(tags);
        
        Boolean active = s.child("is_active").getValue(Boolean.class);
        if (active == null) active = s.child("isActive").getValue(Boolean.class);
        p.setActive(active != null ? active : true);
        
        p.setRating(s.child("rating").getValue(Double.class));
        p.setReviewCount(s.child("review_count").getValue(Integer.class));
        p.setSoldCount(s.child("sold_count").getValue(Integer.class));
        p.setCreatedAt(s.child("created_at").getValue(String.class));
        return p;
    }

    private String slugify(String name) {
        if (name == null) return "";
        String normalized = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD);
        String result = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        result = result.toLowerCase(java.util.Locale.US)
                       .replaceAll("[^a-z0-9\\s-]", "")
                       .replaceAll("\\s+", "-")
                       .replaceAll("-+", "-")
                       .trim();
        return result;
    }

    @Override
    public Call<ApiResponse<List<Product>>> getAllProducts(
            int page, int limit, String search, String category) {
        return new Call<ApiResponse<List<Product>>>() {
            @Override
            public Response<ApiResponse<List<Product>>> execute() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void enqueue(@NonNull Callback<ApiResponse<List<Product>>> callback) {
                dbRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> list = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    if (Boolean.TRUE.equals(s.child("deleted").getValue(Boolean.class))) continue;
                    Product p = mapSnapshotToProduct(s);
                    list.add(p);
                }

                        // Filter by category
                        if (category != null && !category.isEmpty() && !"all".equalsIgnoreCase(category)) {
                            List<Product> filtered = new ArrayList<>();
                            for (Product p : list) {
                                if (category.equalsIgnoreCase(p.getCategory())) {
                                    filtered.add(p);
                                }
                            }
                            list = filtered;
                        }

                        // Filter by search
                        if (search != null && !search.trim().isEmpty()) {
                            String query = search.trim().toLowerCase();
                            List<Product> filtered = new ArrayList<>();
                            for (Product p : list) {
                                if ((p.getName() != null && p.getName().toLowerCase().contains(query)) ||
                                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(query))) {
                                    filtered.add(p);
                                }
                            }
                            list = filtered;
                        }

                        // Paginate
                        int startIndex = (page - 1) * limit;
                        List<Product> paginated = new ArrayList<>();
                        if (startIndex < list.size()) {
                            int endIndex = Math.min(startIndex + limit, list.size());
                            paginated = list.subList(startIndex, endIndex);
                        }

                        callback.onResponse(null, Response.success(ApiResponse.success(paginated, "success", null, null)));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                    }
                });
            }

            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<List<Product>>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<Product> createProduct(Map<String, Object> body) {
        String id = "prod-" + UUID.randomUUID().toString();
        String name = (String) body.get("name");
        String desc = (String) body.get("description");
        
        Double originalPrice = null;
        Object origVal = body.get("originalPrice");
        if (origVal instanceof Number) {
            originalPrice = ((Number) origVal).doubleValue();
        }
        
        Double sellingPrice = null;
        Object sellVal = body.get("sellingPrice");
        if (sellVal instanceof Number) {
            sellingPrice = ((Number) sellVal).doubleValue();
        }
        
        Integer stock = null;
        Object stockVal = body.get("stockQuantity");
        if (stockVal instanceof Number) {
            stock = ((Number) stockVal).intValue();
        }
        
        String unit = (String) body.get("unit");
        String imageUrl = (String) body.get("imageUrl");
        String categoryId = (String) body.get("categoryId");
        boolean active = !body.containsKey("isActive") || Boolean.TRUE.equals(body.get("isActive"));

        Map<String, Object> pMap = new HashMap<>();
        pMap.put("id", id);
        pMap.put("name", name);
        pMap.put("slug", slugify(name));
        pMap.put("description", desc);
        pMap.put("original_price", originalPrice);
        pMap.put("selling_price", sellingPrice);
        pMap.put("stock_quantity", stock);
        pMap.put("unit", unit);
        pMap.put("image_url", imageUrl);
        pMap.put("category_id", categoryId);
        pMap.put("is_active", active);
        pMap.put("isActive", active);
        pMap.put("rating", 5.0);
        pMap.put("review_count", 0);
        pMap.put("sold_count", 0);
        pMap.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(new java.util.Date()));

        final Double finalOriginalPrice = originalPrice;
        final Double finalSellingPrice = sellingPrice;
        final Integer finalStock = stock;
        final boolean finalActive = active;

        return new Call<Product>() {
            @Override public Response<Product> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<Product> callback) {
                dbRef.child("products").child(id).setValue(pMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Product p = new Product();
                        p.setId(id);
                        p.setName(name);
                        p.setSlug(slugify(name));
                        p.setPrice(finalSellingPrice != null ? finalSellingPrice : 0.0);
                        p.setOriginalPrice(finalOriginalPrice);
                        p.setCategory(categoryId);
                        p.setDescription(desc);
                        p.setImageUrl(imageUrl);
                        p.setStock(finalStock != null ? finalStock : 0);
                        p.setUnit(unit);
                        p.setActive(finalActive);
                        callback.onResponse(this, Response.success(p));
                    } else {
                        callback.onFailure(this, new Exception("Firebase write failed"));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<Product> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<Product> updateProduct(String id, Map<String, Object> body) {
        String name = (String) body.get("name");
        String desc = (String) body.get("description");
        
        Double originalPrice = null;
        Object origVal = body.get("originalPrice");
        if (origVal instanceof Number) {
            originalPrice = ((Number) origVal).doubleValue();
        }
        
        Double sellingPrice = null;
        Object sellVal = body.get("sellingPrice");
        if (sellVal instanceof Number) {
            sellingPrice = ((Number) sellVal).doubleValue();
        }
        
        Integer stock = null;
        Object stockVal = body.get("stockQuantity");
        if (stockVal instanceof Number) {
            stock = ((Number) stockVal).intValue();
        }
        
        String unit = (String) body.get("unit");
        String imageUrl = (String) body.get("imageUrl");
        String categoryId = (String) body.get("categoryId");
        Boolean active = body.get("isActive") instanceof Boolean ? (Boolean) body.get("isActive") : null;

        Map<String, Object> updates = new HashMap<>();
        if (name != null) {
            updates.put("name", name);
            updates.put("slug", slugify(name));
        }
        if (desc != null) updates.put("description", desc);
        if (originalPrice != null) updates.put("original_price", originalPrice);
        if (sellingPrice != null) updates.put("selling_price", sellingPrice);
        if (stock != null) updates.put("stock_quantity", stock);
        if (unit != null) updates.put("unit", unit);
        if (imageUrl != null) updates.put("image_url", imageUrl);
        if (categoryId != null) updates.put("category_id", categoryId);
        if (active != null) {
            updates.put("is_active", active);
            updates.put("isActive", active);
        }

        final Double finalOriginalPrice = originalPrice;
        final Double finalSellingPrice = sellingPrice;
        final Integer finalStock = stock;
        final Boolean finalActive = active;

        return new Call<Product>() {
            @Override public Response<Product> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<Product> callback) {
                dbRef.child("products").child(id).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Product p = new Product();
                        p.setId(id);
                        p.setName(name);
                        p.setSlug(slugify(name));
                        p.setPrice(finalSellingPrice != null ? finalSellingPrice : 0.0);
                        p.setOriginalPrice(finalOriginalPrice);
                        p.setCategory(categoryId);
                        p.setDescription(desc);
                        p.setImageUrl(imageUrl);
                        p.setStock(finalStock != null ? finalStock : 0);
                        p.setUnit(unit);
                        p.setActive(finalActive != null ? finalActive : true);
                        callback.onResponse(this, Response.success(p));
                    } else {
                        callback.onFailure(this, new Exception("Firebase update failed"));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<Product> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<Void> deleteProduct(String id) {
        return new Call<Void>() {
            @Override public Response<Void> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<Void> callback) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("is_active", false);
                updates.put("isActive", false);
                updates.put("deleted", true);
                dbRef.child("products").child(id).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResponse(this, Response.success(null));
                    } else {
                        callback.onFailure(this, new Exception("Firebase delete failed"));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<Void> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<UploadResponse>> uploadImage(MultipartBody.Part file) {
        if (retrofit != null) {
            return retrofit.create(AdminProductApi.class).uploadImage(file);
        }
        UploadResponse resp = new UploadResponse();
        try {
            java.lang.reflect.Field field = UploadResponse.class.getDeclaredField("url");
            field.setAccessible(true);
            field.set(resp, "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=300&q=80");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FirebaseCall<>(ApiResponse.success(resp, "success", null, null));
    }

    @Override
    public Call<ApiResponse<List<CategoryResponse>>> getCategories() {
        return new Call<ApiResponse<List<CategoryResponse>>>() {
            @Override public Response<ApiResponse<List<CategoryResponse>>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<List<CategoryResponse>>> callback) {
                dbRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<CategoryResponse> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            CategoryResponse cr = new CategoryResponse();
                            cr.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
                            cr.setName(s.child("name").getValue(String.class));
                            cr.setSlug(s.child("slug").getValue(String.class));
                            cr.setImageUrl(s.child("image_url").getValue(String.class));
                            cr.setParentId(s.child("parent_id").getValue(String.class));
                            cr.setParentName(s.child("parent_name").getValue(String.class));
                            
                            Boolean active = s.child("is_active").getValue(Boolean.class);
                            if (active == null) active = s.child("isActive").getValue(Boolean.class);
                            cr.setActive(active != null ? active : true);
                            
                            list.add(cr);
                        }
                        callback.onResponse(null, Response.success(ApiResponse.success(list, "success", null, null)));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<List<CategoryResponse>>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ResponseBody> exportProducts() {
        return new FirebaseCall<>(ResponseBody.create("id,name,price\n", okhttp3.MediaType.parse("text/csv")));
    }
}
