package vn.vuavuive.admin.data.firebase;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.ProductApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.Review;
import vn.vuavuive.shared.data.dto.request.ReviewRequest;

public class FirebaseProductApi implements ProductApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

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

    @Override
    public Call<ApiResponse<List<Product>>> getProducts(
            String category, String search, int page, int limit, String sort) {
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

                        // Sort
                        if (sort != null) {
                            if (sort.equalsIgnoreCase("price_asc")) {
                                list.sort(Comparator.comparingDouble(Product::getPrice));
                            } else if (sort.equalsIgnoreCase("price_desc")) {
                                list.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                            }
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
                        callback.onResponse(null, Response.success(ApiResponse.error("Database error: " + error.getMessage())));
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
    public Call<ApiResponse<List<String>>> getCategories() {
        return new Call<ApiResponse<List<String>>>() {
            @Override
            public Response<ApiResponse<List<String>>> execute() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void enqueue(@NonNull Callback<ApiResponse<List<String>>> callback) {
                dbRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            String slug = s.child("slug").getValue(String.class);
                            if (slug != null) {
                                list.add(slug);
                            }
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
            @NonNull @Override public Call<ApiResponse<List<String>>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Product>> getProduct(String id) {
        return getProductDetail(id);
    }

    @Override
    public Call<ApiResponse<Product>> getProductDetail(String id) {
        return new Call<ApiResponse<Product>>() {
            @Override
            public Response<ApiResponse<Product>> execute() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Product>> callback) {
                dbRef.child("products").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Product p = mapSnapshotToProduct(snapshot);
                            callback.onResponse(null, Response.success(ApiResponse.success(p, "success", null, null)));
                        } else {
                            callback.onResponse(null, Response.success(ApiResponse.error("Product not found")));
                        }
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
            @NonNull @Override public Call<ApiResponse<Product>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<List<Review>>> getProductReviews(String id) {
        return new FirebaseCall<>(ApiResponse.success(Collections.emptyList(), "success", null, null));
    }

    @Override
    public Call<ApiResponse<Review>> submitReview(ReviewRequest body) {
        return new FirebaseCall<>(ApiResponse.error("Not supported in Admin"));
    }
}
