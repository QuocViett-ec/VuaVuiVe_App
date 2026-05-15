package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.User;

public interface AdminUserApi {

    @GET("api/users/users")
    Call<ApiResponse<List<User>>> getUsers(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("search") String search,
            @Query("role") String role
    );

    @GET("api/users/users/{id}")
    Call<ApiResponse<User>> getUser(@Path("id") String id);

    @PUT("api/users/users/{id}")
    Call<ApiResponse<User>> updateUser(
            @Path("id") String id,
            @Body Map<String, Object> body
    );

    @DELETE("api/users/users/{id}")
    Call<ApiResponse<Void>> deleteUser(@Path("id") String id);

    @GET("api/admin/users/export")
    @Streaming
    Call<ResponseBody> exportUsers();
}
