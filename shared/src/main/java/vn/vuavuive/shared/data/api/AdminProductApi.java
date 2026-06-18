package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
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
import vn.vuavuive.shared.data.dto.CategoryResponse;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.UploadResponse;

public interface AdminProductApi {

    @GET("api/products")
    Call<ApiResponse<List<Product>>> getAllProducts(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("search") String search,
            @Query("category") String category
    );

    @POST("api/products")
    Call<Product> createProduct(@Body Map<String, Object> body);

    @PUT("api/products/{id}")
    Call<Product> updateProduct(
            @Path("id") String id,
            @Body Map<String, Object> body
    );

    @DELETE("api/products/{id}")
    Call<Void> deleteProduct(@Path("id") String id);

    @Multipart
    @POST("api/uploads/images")
    Call<ApiResponse<UploadResponse>> uploadImage(@Part MultipartBody.Part file);

    @GET("api/categories")
    Call<ApiResponse<List<CategoryResponse>>> getCategories();

    @GET("api/admin/products/export")
    @Streaming
    Call<ResponseBody> exportProducts();
}
