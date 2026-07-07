package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.Review;
import vn.vuavuive.shared.data.dto.Voucher;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest;

public interface OrderApi {

    @POST("api/orders")
    Call<ApiResponse<Order>> createOrder(@Body CreateOrderRequest body);

    @GET("api/orders/me")
    Call<ApiResponse<List<Order>>> getMyOrders(
            @Query("status") String status,
            @Query("page") int page,
            @Query("limit") int limit
    );

    @GET("api/orders/{id}")
    Call<ApiResponse<Order>> getOrderDetail(@Path("id") String id);

    @PATCH("api/orders/{id}/cancel")
    Call<ApiResponse<Order>> cancelOrder(@Path("id") String id);

    @POST("api/orders/{id}/return-request")
    Call<ApiResponse<Order>> returnRequest(
            @Path("id") String id,
            @Body Map<String, String> body
    );

    @POST("api/orders/{id}/reviews")
    Call<ApiResponse<Review>> submitReview(
            @Path("id") String id,
            @Body Map<String, Object> body
    );

    @GET("api/orders/{id}/reviews/me")
    Call<ApiResponse<Review>> getMyReview(@Path("id") String id);

    @GET("api/orders/voucher/available")
    Call<ApiResponse<List<Voucher>>> getAvailableVouchers();

    @POST("api/orders/voucher/validate")
    Call<ApiResponse<Voucher>> validateVoucher(@Body Map<String, String> body);
}
