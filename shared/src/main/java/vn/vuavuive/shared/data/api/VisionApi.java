package vn.vuavuive.shared.data.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.VisionSearchResponse;

public interface VisionApi {
    @Multipart
    @POST("api/vision/product-search")
    Call<ApiResponse<VisionSearchResponse>> searchProductByImage(@Part MultipartBody.Part image);
}
