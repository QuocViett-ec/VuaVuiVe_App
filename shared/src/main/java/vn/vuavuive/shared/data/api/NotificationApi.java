package vn.vuavuive.shared.data.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import vn.vuavuive.shared.data.dto.ApiResponse;

public interface NotificationApi {

    @POST("api/notifications/device-token")
    Call<ApiResponse<Void>> registerDeviceToken(@Body Map<String, String> body);

    @HTTP(method = "DELETE", path = "api/notifications/device-token", hasBody = true)
    Call<ApiResponse<Void>> deleteDeviceToken(@Body Map<String, String> body);
}
