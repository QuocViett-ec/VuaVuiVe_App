package vn.vuavuive.shared.data.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.CreateMomoPaymentRequest;
import vn.vuavuive.shared.data.dto.CreateMomoPaymentResponse;
import vn.vuavuive.shared.data.dto.PaymentStatusResponse;

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

    @POST("api/momo/create-payment")
    Call<ApiResponse<CreateMomoPaymentResponse>> createMomoPayment(@Body CreateMomoPaymentRequest request);

    @GET("api/payments/{orderId}/status")
    Call<ApiResponse<PaymentStatusResponse>> getPaymentStatus(@Path("orderId") String orderId);
}
