package vn.vuavuive.customer.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.ProductApi;
import vn.vuavuive.shared.data.api.RecommendApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.Review;
import vn.vuavuive.shared.data.dto.request.ReviewRequest;
import vn.vuavuive.shared.data.local.ProductDao;
import vn.vuavuive.shared.data.local.ProductEntity;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ProductRepository {

    private final ProductApi productApi;
    private final RecommendApi recommendApi;
    private final ProductDao productDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public ProductRepository(ProductApi productApi, RecommendApi recommendApi, ProductDao productDao) {
        this.productApi = productApi;
        this.recommendApi = recommendApi;
        this.productDao = productDao;
    }

    // ── Product List ───────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<List<Product>>> getProducts(
            String category, String search, int page, int limit, String sort) {

        MutableLiveData<AuthRepository.Result<List<Product>>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        productApi.getProducts(category, search, page, limit, sort)
                .enqueue(new Callback<ApiResponse<List<Product>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Product>>> call,
                                           Response<ApiResponse<List<Product>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            List<Product> products = response.body().getData();
                            // Cache to Room
                            if (page == 1 && products != null) {
                                cacheProducts(products);
                            }
                            result.postValue(AuthRepository.Result.success(products));
                        } else {
                            // Try Room cache on failure
                            loadFromCache(result);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                        loadFromCache(result);
                    }
                });
        return result;
    }

    // ── Product Detail ─────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Product>> getProductDetail(String productId) {
        MutableLiveData<AuthRepository.Result<Product>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        productApi.getProductDetail(productId).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.error("Không tìm thấy sản phẩm"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Product Reviews ────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<List<Review>>> getProductReviews(String productId) {
        MutableLiveData<AuthRepository.Result<List<Review>>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        productApi.getProductReviews(productId).enqueue(new Callback<ApiResponse<List<Review>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Review>>> call, Response<ApiResponse<List<Review>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.success(new ArrayList<>()));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Review>>> call, Throwable t) {
                result.postValue(AuthRepository.Result.success(new ArrayList<>()));
            }
        });
        return result;
    }

    // ── Similar Products ───────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<List<Product>>> getSimilarProducts(String productId) {
        MutableLiveData<AuthRepository.Result<List<Product>>> result = new MutableLiveData<>();

        recommendApi.getSimilarProducts(productId).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.success(new ArrayList<>()));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                result.postValue(AuthRepository.Result.success(new ArrayList<>()));
            }
        });
        return result;
    }

    // ── Recommendations ───────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<List<Product>>> getRecommendations(String userId, int n) {
        MutableLiveData<AuthRepository.Result<List<Product>>> result = new MutableLiveData<>();

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        if (userId != null && !userId.isEmpty()) {
            body.put("user_id", userId);
        }
        body.put("n", n);

        recommendApi.getRecommendations(body).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call,
                                   Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.success(new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                result.postValue(AuthRepository.Result.success(new ArrayList<>()));
            }
        });
        return result;
    }

    public LiveData<AuthRepository.Result<Void>> sendRecommendEvent(
            String eventType, String productId, java.util.Map<String, Object> metadata) {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("eventType", eventType);
        body.put("productId", productId);
        if (metadata != null && !metadata.isEmpty()) {
            body.put("metadata", metadata);
        }

        recommendApi.sendEvent(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                result.postValue(AuthRepository.Result.success(null));
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error(t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<AuthRepository.Result<Review>> submitProductReview(String productId, int rating, String comment) {
        MutableLiveData<AuthRepository.Result<Review>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        ReviewRequest req = new ReviewRequest(productId, rating, comment);

        productApi.submitReview(req).enqueue(new Callback<ApiResponse<Review>>() {
            @Override
            public void onResponse(Call<ApiResponse<Review>> call, Response<ApiResponse<Review>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    String msg = "Không thể gửi đánh giá";
                    if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            JSONObject obj = new JSONObject(errStr);
                            if (obj.has("message")) msg = obj.getString("message");
                        } catch (Exception ignored) {}
                    }
                    result.postValue(AuthRepository.Result.error(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Review>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Room Cache Helpers ─────────────────────────────────────────────────
    private void cacheProducts(List<Product> products) {
        executor.execute(() -> {
            productDao.clearAll();
            List<ProductEntity> entities = new ArrayList<>();
            for (Product p : products) {
                entities.add(ProductEntity.fromProduct(p));
            }
            productDao.insertAll(entities);
        });
    }

    private void loadFromCache(MutableLiveData<AuthRepository.Result<List<Product>>> result) {
        executor.execute(() -> {
            List<ProductEntity> cached = productDao.getProductsSync();
            if (cached != null && !cached.isEmpty()) {
                List<Product> products = new ArrayList<>();
                for (ProductEntity e : cached) products.add(e.toProduct());
                result.postValue(AuthRepository.Result.success(products));
            } else {
                result.postValue(AuthRepository.Result.error("Không có kết nối mạng"));
            }
        });
    }
}
