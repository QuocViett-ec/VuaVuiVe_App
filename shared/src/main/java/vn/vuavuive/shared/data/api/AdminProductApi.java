package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Product;

public interface AdminProductApi {

    @GET("api/admin/products")
    Call<ApiResponse<List<Product>>> getAllProducts(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("search") String search,
            @Query("category") String category
    );

    @Multipart
    @POST("api/products")
    Call<ApiResponse<Product>> createProduct(
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("category") RequestBody category,
            @Part("description") RequestBody description,
            @Part("stock") RequestBody stock,
            @Part("unit") RequestBody unit,
            @Part MultipartBody.Part image
    );

    @Multipart
    @PUT("api/products/{id}")
    Call<ApiResponse<Product>> updateProduct(
            @Path("id") String id,
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("category") RequestBody category,
            @Part("description") RequestBody description,
            @Part("stock") RequestBody stock,
            @Part("unit") RequestBody unit,
            @Part MultipartBody.Part image
    );

    @DELETE("api/products/{id}")
    Call<ApiResponse<Void>> deleteProduct(@Path("id") String id);

    @GET("api/admin/products/export")
    @Streaming
    Call<ResponseBody> exportProducts();
}
