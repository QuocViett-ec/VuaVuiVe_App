package vn.vuavuive.shared.data.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.request.GoogleLoginRequest;
import vn.vuavuive.shared.data.dto.request.LoginRequest;
import vn.vuavuive.shared.data.dto.request.RegisterRequest;
import java.util.Map;

public interface AuthApi {

    @POST("api/auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest body);

    @POST("api/auth/login")
    Call<ApiResponse<User>> login(@Body LoginRequest body);

    @POST("api/auth/admin/login")
    Call<ApiResponse<User>> adminLogin(@Body LoginRequest body);

    @POST("api/auth/google")
    Call<ApiResponse<User>> googleLogin(@Body GoogleLoginRequest body);

    @POST("api/auth/logout")
    Call<ApiResponse<Void>> logout();

    @GET("api/auth/me")
    Call<ApiResponse<User>> getMe();

    @PUT("api/auth/profile")
    Call<ApiResponse<User>> updateProfile(@Body Map<String, Object> body);

    @PUT("api/auth/password")
    Call<ApiResponse<Void>> changePassword(@Body Map<String, String> body);

    @POST("api/auth/set-local-password")
    Call<ApiResponse<Void>> setLocalPassword(@Body Map<String, String> body);

    @POST("api/auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body Map<String, String> body);

    @POST("api/auth/verify-otp")
    Call<ApiResponse<Void>> verifyOtp(@Body Map<String, String> body);

    @POST("api/auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body Map<String, String> body);
}
