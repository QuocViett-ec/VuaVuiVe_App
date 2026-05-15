package vn.vuavuive.shared.data.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import vn.vuavuive.shared.data.dto.ApiResponse;

public interface PaymentApi {

    /**
     * Tạo URL VNPay để mở trong WebView.
     * Body: { orderId: String, returnUrl: String }
     */
    @POST("api/payment/vnpay/create")
    Call<ApiResponse<Map<String, String>>> createVNPayUrl(@Body Map<String, String> body);

    /**
     * Tạo URL MoMo để mở trong WebView.
     * Body: { orderId: String, returnUrl: String }
     */
    @POST("api/payment/momo/create")
    Call<ApiResponse<Map<String, String>>> createMoMoUrl(@Body Map<String, String> body);
}
