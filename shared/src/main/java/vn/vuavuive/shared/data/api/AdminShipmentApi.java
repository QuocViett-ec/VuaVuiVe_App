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
import vn.vuavuive.shared.data.dto.Shipment;

public interface AdminShipmentApi {

    @GET("api/shipments")
    Call<ApiResponse<List<Shipment>>> getAllShipments(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("status") String status
    );

    @POST("api/shipments")
    Call<ApiResponse<Shipment>> createShipment(@Body Map<String, Object> body);

    @PATCH("api/shipments/{id}")
    Call<ApiResponse<Shipment>> updateShipment(
            @Path("id") String id,
            @Body Map<String, Object> body
    );
}
