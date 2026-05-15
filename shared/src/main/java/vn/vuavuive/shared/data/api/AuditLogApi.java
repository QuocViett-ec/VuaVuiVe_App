package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import vn.vuavuive.shared.data.dto.ApiResponse;

public interface AuditLogApi {

    @GET("api/users/audit-logs")
    Call<ApiResponse<List<Map<String, Object>>>> getAuditLogs(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("action") String action,
            @Query("adminId") String adminId,
            @Query("from") String from,
            @Query("to") String to
    );

    @POST("api/users/audit-logs")
    Call<ApiResponse<Void>> createAuditLog(@Body Map<String, Object> body);
}
