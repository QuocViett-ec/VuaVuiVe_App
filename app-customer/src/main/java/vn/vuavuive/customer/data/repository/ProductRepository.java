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
import vn.vuavuive.shared.data.local.ProductDao;
import vn.vuavuive.shared.data.local.ProductEntity;
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
