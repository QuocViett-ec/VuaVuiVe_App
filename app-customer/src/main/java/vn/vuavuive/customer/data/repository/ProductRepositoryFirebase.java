package vn.vuavuive.customer.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.Review;
import vn.vuavuive.shared.data.dto.request.ReviewRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ProductRepositoryFirebase {

    private final DatabaseReference dbRef;
    private java.util.Map<String, String> slugToIdMap = null;

    @Inject
    public ProductRepositoryFirebase() {
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    private Double getSafeDouble(DataSnapshot childSnap) {
        if (!childSnap.exists()) return null;
        Object val = childSnap.getValue();
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer getSafeInt(DataSnapshot childSnap) {
        if (!childSnap.exists()) return null;
        Object val = childSnap.getValue();
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // ── Map Firebase Snapshot to Product Model ──────────────────────────────
    private Product mapSnapshotToProduct(DataSnapshot s) {
        Product p = new Product();
        p.setId(s.child("id").getValue(String.class));
        p.setName(s.child("name").getValue(String.class));
        p.setSlug(s.child("slug").getValue(String.class));
        
        Double sellingPrice = getSafeDouble(s.child("selling_price"));
        if (sellingPrice == null) {
            sellingPrice = getSafeDouble(s.child("sellingPrice"));
        }
        p.setPrice(sellingPrice != null ? sellingPrice : 0.0);
        
        Double originalPrice = getSafeDouble(s.child("original_price"));
        if (originalPrice == null) {
            originalPrice = getSafeDouble(s.child("originalPrice"));
        }
        p.setOriginalPrice(originalPrice);
        
        String categoryId = s.child("category_id").getValue(String.class);
        if (categoryId == null) {
            categoryId = s.child("categoryId").getValue(String.class);
        }
        p.setCategory(categoryId);
        
        p.setSubCategory(s.child("sub_category").getValue(String.class));
        p.setDescription(s.child("description").getValue(String.class));
        
        String imageUrl = s.child("image_url").getValue(String.class);
        if (imageUrl == null) {
            imageUrl = s.child("imageUrl").getValue(String.class);
        }
        p.setImageUrl(imageUrl);
        
        Integer stock = getSafeInt(s.child("stock_quantity"));
        if (stock == null) {
            stock = getSafeInt(s.child("stockQuantity"));
        }
        if (stock == null) {
            stock = getSafeInt(s.child("stock"));
        }
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
        if (active == null) {
            active = s.child("isActive").getValue(Boolean.class);
        }
        p.setActive(active != null ? active : false);
        
        p.setRating(getSafeDouble(s.child("rating")));
        
        Integer reviewCount = getSafeInt(s.child("review_count"));
        if (reviewCount == null) {
            reviewCount = getSafeInt(s.child("reviewCount"));
        }
        p.setReviewCount(reviewCount);
        
        Integer soldCount = getSafeInt(s.child("sold_count"));
        if (soldCount == null) {
            soldCount = getSafeInt(s.child("soldCount"));
        }
        p.setSoldCount(soldCount);
        
        String createdAt = s.child("created_at").getValue(String.class);
        if (createdAt == null) {
            createdAt = s.child("createdAt").getValue(String.class);
        }
        p.setCreatedAt(createdAt);
        return p;
    }

    // ── Map Firebase Snapshot to Review Model ───────────────────────────────
    private Review mapSnapshotToReview(DataSnapshot s) {
        Review r = new Review();
        r.setId(s.child("id").getValue(String.class));
        r.setProductId(s.child("product_id").getValue(String.class));
        r.setUserId(s.child("user_id").getValue(String.class));
        r.setUserName(s.child("user_name").getValue(String.class));
        r.setRating(s.child("rating").getValue(Integer.class));
        r.setComment(s.child("comment").getValue(String.class));
        r.setCreatedAt(s.child("created_at").getValue(String.class));
        return r;
    }

    // ── Get Product List (filtered & sorted on client side) ─────────────────
    public LiveData<AuthRepository.Result<List<Product>>> getProducts(
            String category, String search, int page, int limit, String sort) {

        MutableLiveData<AuthRepository.Result<List<Product>>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        if (slugToIdMap != null) {
            fetchProductsFromFirebase(category, search, page, limit, sort, result);
        } else {
            dbRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        String id = s.child("id").getValue(String.class);
                        String slug = s.child("slug").getValue(String.class);
                        if (id != null && slug != null) {
                            map.put(slug, id);
                        }
                    }
                    slugToIdMap = map;
                    fetchProductsFromFirebase(category, search, page, limit, sort, result);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    fetchProductsFromFirebase(category, search, page, limit, sort, result);
                }
            });
        }

        return result;
    }

    private void fetchProductsFromFirebase(
            String category, String search, int page, int limit, String sort,
            MutableLiveData<AuthRepository.Result<List<Product>>> result) {

        dbRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                android.util.Log.d("ProductRepositoryFirebase", "onDataChange: snapshot.getChildrenCount() = " + snapshot.getChildrenCount());
                List<Product> products = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Product p = mapSnapshotToProduct(s);
                    if (p.isActive()) {
                        products.add(p);
                    }
                }
                android.util.Log.d("ProductRepositoryFirebase", "onDataChange: mapped " + products.size() + " active products.");

                // 1. Filter by category (by id or slug)
                if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("Tất cả") && !category.equalsIgnoreCase("all")) {
                    List<Product> filtered = new ArrayList<>();
                    String targetId = slugToIdMap != null ? slugToIdMap.get(category) : null;
                    for (Product p : products) {
                        if (category.equals(p.getCategory()) || (targetId != null && targetId.equals(p.getCategory()))) {
                            filtered.add(p);
                        }
                    }
                    products = filtered;
                }

                // 2. Filter by search query (Accent-insensitive matching)
                if (search != null && !search.trim().isEmpty()) {
                    String query = deAccent(search.trim()).toLowerCase();
                    List<Product> filtered = new ArrayList<>();
                    for (Product p : products) {
                        if (p.getName() != null) {
                            String nameNormalized = deAccent(p.getName()).toLowerCase();
                            if (nameNormalized.contains(query)) {
                                filtered.add(p);
                            }
                        }
                    }
                    products = filtered;
                }

                // 3. Sort
                if (sort != null) {
                    if (sort.equals("price_asc")) {
                        products.sort(Comparator.comparingDouble(Product::getPrice));
                    } else if (sort.equals("price_desc")) {
                        products.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                    } else if (sort.equals("rating")) {
                        products.sort((p1, p2) -> {
                            double r1 = p1.getRating() != null ? p1.getRating() : 0.0;
                            double r2 = p2.getRating() != null ? p2.getRating() : 0.0;
                            return Double.compare(r2, r1);
                        });
                    } else if (sort.equals("newest")) {
                        products.sort((p1, p2) -> {
                            String t1 = p1.getCreatedAt() != null ? p1.getCreatedAt() : "";
                            String t2 = p2.getCreatedAt() != null ? p2.getCreatedAt() : "";
                            return t2.compareTo(t1); // Descending order
                        });
                    }
                }

                // 4. Pagination
                int startIndex = (page - 1) * limit;
                if (startIndex >= products.size()) {
                    result.postValue(AuthRepository.Result.success(new ArrayList<>()));
                } else {
                    int endIndex = Math.min(startIndex + limit, products.size());
                    result.postValue(AuthRepository.Result.success(products.subList(startIndex, endIndex)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.error("Lỗi tải sản phẩm: " + error.getMessage()));
            }
        });
    }

    // ── Get Product Detail ──────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Product>> getProductDetail(String productId) {
        MutableLiveData<AuthRepository.Result<Product>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        dbRef.child("products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    result.postValue(AuthRepository.Result.success(mapSnapshotToProduct(snapshot)));
                } else {
                    result.postValue(AuthRepository.Result.error("Không tìm thấy sản phẩm"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối Firebase: " + error.getMessage()));
            }
        });

        return result;
    }

    // ── Get Product Reviews ─────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<List<Review>>> getProductReviews(String productId) {
        MutableLiveData<AuthRepository.Result<List<Review>>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        dbRef.child("reviews").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Review> reviews = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Review r = mapSnapshotToReview(s);
                    if (productId.equals(r.getProductId())) {
                        reviews.add(r);
                    }
                }
                result.postValue(AuthRepository.Result.success(reviews));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.success(new ArrayList<>()));
            }
        });

        return result;
    }

    // ── Get Similar Products (Mock based on Category) ────────────────────────
    public LiveData<AuthRepository.Result<List<Product>>> getSimilarProducts(String productId) {
        MutableLiveData<AuthRepository.Result<List<Product>>> result = new MutableLiveData<>();

        dbRef.child("products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot detailSnapshot) {
                if (!detailSnapshot.exists()) {
                    result.postValue(AuthRepository.Result.success(new ArrayList<>()));
                    return;
                }
                String categoryId = detailSnapshot.child("category_id").getValue(String.class);
                
                dbRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot listSnapshot) {
                        List<Product> list = new ArrayList<>();
                        for (DataSnapshot s : listSnapshot.getChildren()) {
                            Product p = mapSnapshotToProduct(s);
                            if (categoryId != null && categoryId.equals(p.getCategory()) && !productId.equals(p.getId())) {
                                list.add(p);
                            }
                        }
                        result.postValue(AuthRepository.Result.success(list));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        result.postValue(AuthRepository.Result.success(new ArrayList<>()));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.success(new ArrayList<>()));
            }
        });

        return result;
    }

    // ── Get Recommendations (Mock first N items) ───────────────────────────
    public LiveData<AuthRepository.Result<List<Product>>> getRecommendations(String userId, int n) {
        MutableLiveData<AuthRepository.Result<List<Product>>> result = new MutableLiveData<>();

        dbRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> list = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    list.add(mapSnapshotToProduct(s));
                }
                Collections.shuffle(list);
                int count = Math.min(n, list.size());
                result.postValue(AuthRepository.Result.success(list.subList(0, count)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.success(new ArrayList<>()));
            }
        });

        return result;
    }

    // ── Send Recommendation Event (Mock) ───────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> sendRecommendEvent(
            String eventType, String productId, Map<String, Object> metadata) {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.success(null));
        return result;
    }

    // ── Submit Product Review ───────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Review>> submitProductReview(String productId, int rating, String comment) {
        MutableLiveData<AuthRepository.Result<Review>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        String reviewId = UUID.randomUUID().toString();
        Map<String, Object> revData = new HashMap<>();
        revData.put("id", reviewId);
        revData.put("product_id", productId);
        revData.put("rating", rating);
        revData.put("comment", comment);
        
        // Use Firebase Auth user or fallback
        com.google.firebase.auth.FirebaseUser fbUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbUser != null ? fbUser.getUid() : "guest_user";
        revData.put("user_id", uid);
        
        // Fetch User profile name if available, otherwise default
        if (fbUser != null) {
            dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String fullName = snapshot.child("full_name").getValue(String.class);
                    revData.put("user_name", fullName != null ? fullName : fbUser.getEmail());
                    saveReviewToFirebase(reviewId, revData, result);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    revData.put("user_name", "Anonymous");
                    saveReviewToFirebase(reviewId, revData, result);
                }
            });
        } else {
            revData.put("user_name", "Khách");
            saveReviewToFirebase(reviewId, revData, result);
        }

        return result;
    }

    private void saveReviewToFirebase(String id, Map<String, Object> data, MutableLiveData<AuthRepository.Result<Review>> result) {
        data.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(new java.util.Date()));
        dbRef.child("reviews").child(id).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Review r = new Review();
                r.setId(id);
                r.setProductId((String) data.get("product_id"));
                r.setUserId((String) data.get("user_id"));
                r.setUserName((String) data.get("user_name"));
                r.setRating((Integer) data.get("rating"));
                r.setComment((String) data.get("comment"));
                r.setCreatedAt((String) data.get("created_at"));
                result.postValue(AuthRepository.Result.success(r));
            } else {
                result.postValue(AuthRepository.Result.error(task.getException() != null ? task.getException().getMessage() : "Failed to save review"));
            }
        });
    }

    private static String deAccent(String str) {
        if (str == null) return "";
        String nfdNormalizedString = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(nfdNormalizedString).replaceAll("");
        return result.replace('đ', 'd').replace('Đ', 'D');
    }
}
