package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Voucher;

public interface AdminVoucherApi {

    @GET("api/admin/vouchers")
    Call<ApiResponse<List<Voucher>>> getVouchers();

    @POST("api/admin/vouchers")
    Call<ApiResponse<Voucher>> createVoucher(@Body Map<String, Object> body);

    @PUT("api/admin/vouchers/{code}")
    Call<ApiResponse<Voucher>> updateVoucher(
            @Path("code") String code,
            @Body Map<String, Object> body
    );

    @DELETE("api/admin/vouchers/{code}")
    Call<ApiResponse<Void>> deleteVoucher(@Path("code") String code);
}
