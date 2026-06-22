package vn.vuavuive.shared.data.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.Review;
import vn.vuavuive.shared.data.dto.request.ReviewRequest;

public interface ProductApi {

    @GET("api/products")
    Call<ApiResponse<List<Product>>> getProducts(
            @Query("category") String category,
            @Query("search") String search,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("sort") String sort
    );

    @GET("api/products/categories")
    Call<ApiResponse<List<String>>> getCategories();

    @GET("api/products/{id}")
    Call<ApiResponse<Product>> getProduct(@Path("id") String id);

    /** Alias used by ProductRepository */
    @GET("api/products/{id}")
    Call<ApiResponse<Product>> getProductDetail(@Path("id") String id);

    @GET("api/products/{id}/reviews")
    Call<ApiResponse<List<Review>>> getProductReviews(@Path("id") String id);

    @POST("api/reviews")
    Call<ApiResponse<Review>> submitReview(@Body ReviewRequest body);
}

