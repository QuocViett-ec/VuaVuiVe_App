package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.ShipperProfile;

/**
 * ShipperOrderApi — Các endpoint dành riêng cho ứng dụng Shipper.
 * SHIPPER: Xem đơn được gán, cập nhật trạng thái giao hàng.
 */
public interface ShipperOrderApi {

    @GET("api/shippers/me")
    Call<ApiResponse<ShipperProfile>> getMyProfile();

    /** Lấy danh sách đơn hàng được gán cho Shipper này (lọc theo trạng thái) */
    @GET("api/orders/shipper")
    Call<ApiResponse<List<Order>>> getMyShipperOrders(
            @Query("status") String status  // PREPARING, IN_TRANSIT, DELIVERED, FAILED
    );

    /** Shipper cập nhật trạng thái giao hàng của một đơn */
    @PUT("api/shippers/{shipperId}/orders/{orderId}/delivery")
    Call<ApiResponse<Void>> updateDeliveryStatus(
            @Path("shipperId") String shipperId,
            @Path("orderId") String orderId,
            @Query("status") String status,
            @Query("note") String note
    );

    /** Shipper bật/tắt trạng thái hoạt động (AVAILABLE / OFFLINE) */
    @PUT("api/shippers/{shipperId}/status")
    Call<ApiResponse<Void>> updateShipperStatus(
            @Path("shipperId") String shipperId,
            @Query("status") String status
    );
}
