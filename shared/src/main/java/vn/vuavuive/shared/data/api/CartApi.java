package vn.vuavuive.shared.data.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Cart;
import java.util.Map;

public interface CartApi {

    @GET("api/cart/me")
    Call<ApiResponse<Cart>> getCart();

    @PUT("api/cart/me")
    Call<ApiResponse<Cart>> syncCart(@Body Map<String, Object> body);

    @POST("api/cart/me/merge")
    Call<ApiResponse<Cart>> mergeCart(@Body Map<String, Object> body);

    @DELETE("api/cart/me")
    Call<ApiResponse<Void>> clearCart();
}
