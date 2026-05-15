package vn.vuavuive.shared.data.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Shipment;

public interface ShipmentApi {

    @GET("api/shipments/me")
    Call<ApiResponse<List<Shipment>>> getMyShipments();

    @GET("api/shipments/{id}")
    Call<ApiResponse<Shipment>> getShipment(@Path("id") String id);
}
