package vn.vuavuive.shared.data.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;

public interface OrderStatusApi {

    @PATCH("api/orders/{id}/status")
    Call<ApiResponse<Order>> updateStatus(
            @Path("id") String orderId,
            @Body Map<String, String> body);

    @POST("api/shippers/{shipperId}/assign/{orderId}")
    Call<ApiResponse<Map<String, String>>> assignShipper(
            @Path("orderId") String orderId,
            @Path("shipperId") String shipperId);

    @PUT("api/orders/{id}/return-review")
    Call<ApiResponse<Order>> reviewReturn(
            @Path("id") String orderId,
            @Body Map<String, String> body);

    @PATCH("api/orders/{id}/paid")
    Call<ApiResponse<Order>> markPaid(@Path("id") String orderId);

    @PATCH("api/orders/{id}/refund")
    Call<ApiResponse<Order>> markRefunded(@Path("id") String orderId);
}
