package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import okhttp3.ResponseBody;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;

public interface AdminOrderApi {

    @GET("api/admin/orders")
    Call<ApiResponse<List<Order>>> getOrders(
            @Query("status") String status,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("from") String from,
            @Query("to") String to
    );

    @PATCH("api/admin/orders/bulk-status")
    Call<ApiResponse<Void>> bulkUpdateStatus(@Body Map<String, Object> body);

    @GET("api/admin/orders/export")
    @Streaming
    Call<ResponseBody> exportOrders();

    @POST("api/shippers/{shipperId}/assign/{orderId}")
    Call<Map<String, String>> assignShipper(
            @Path("orderId") String orderId,
            @Path("shipperId") String shipperId
    );

    @PUT("api/orders/{id}/status")
    Call<ApiResponse<Order>> updateOrderStatus(
            @Path("id") String id,
            @Body Map<String, String> body
    );

    @PUT("api/orders/{id}/return-review")
    Call<ApiResponse<Order>> reviewReturnRequest(
            @Path("id") String id,
            @Body Map<String, String> body
    );

    @PATCH("api/orders/{id}/paid")
    Call<ApiResponse<Order>> markPaid(@Path("id") String id);

    @PATCH("api/orders/{id}/refund")
    Call<ApiResponse<Order>> markRefunded(@Path("id") String id);
}
