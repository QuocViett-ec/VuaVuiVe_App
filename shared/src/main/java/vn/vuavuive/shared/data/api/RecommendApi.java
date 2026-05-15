package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Product;

public interface RecommendApi {

    @POST("api/recommend")
    Call<ApiResponse<List<Product>>> getRecommendations(@Body Map<String, Object> body);

    @POST("api/recommend/event")
    Call<ApiResponse<Void>> sendEvent(@Body Map<String, Object> body);

    @GET("api/recommend/similar/{id}")
    Call<ApiResponse<List<Product>>> getSimilarProducts(@Path("id") String id);

    @POST("api/recommend/similar-ml")
    Call<ApiResponse<List<Product>>> getSimilarProductsML(@Body Map<String, String> body);

    @GET("api/recommend/history")
    Call<ApiResponse<List<Map<String, Object>>>> getRecommendHistory();
}
