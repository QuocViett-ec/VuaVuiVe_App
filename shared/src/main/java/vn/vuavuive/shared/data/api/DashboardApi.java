package vn.vuavuive.shared.data.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.DashboardStats;

public interface DashboardApi {

    @GET("api/users/dashboard/stats")
    Call<ApiResponse<DashboardStats>> getStats();

    @GET("api/users/dashboard/analytics")
    Call<ApiResponse<Map<String, Object>>> getAnalytics(
            @Query("from") String from,
            @Query("to") String to
    );
}
