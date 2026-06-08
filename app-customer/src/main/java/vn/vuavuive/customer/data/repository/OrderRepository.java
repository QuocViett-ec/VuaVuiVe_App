package vn.vuavuive.customer.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.api.PaymentApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.Voucher;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OrderRepository {

    private final OrderApi orderApi;
    private final PaymentApi paymentApi;

    @Inject
    public OrderRepository(OrderApi orderApi, PaymentApi paymentApi) {
        this.orderApi = orderApi;
        this.paymentApi = paymentApi;
    }

    // ── Order List ─────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<List<Order>>> getOrders(String status, int page, int limit) {
        MutableLiveData<AuthRepository.Result<List<Order>>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        orderApi.getMyOrders(status, page, limit).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.error("Không thể tải danh sách đơn hàng"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Order Detail ───────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Order>> getOrderDetail(String orderId) {
        MutableLiveData<AuthRepository.Result<Order>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        orderApi.getOrderDetail(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.error("Không tìm thấy đơn hàng"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối"));
            }
        });
        return result;
    }

    // ── Create Order ───────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Order>> createOrder(CreateOrderRequest request) {
        MutableLiveData<AuthRepository.Result<Order>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        orderApi.createOrder(request).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    String msg = "Đặt hàng thất bại";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            String err = response.errorBody().string();
                            // Try parse JSON message field
                            if (err.contains("\"message\"")) {
                                int s = err.indexOf("\"message\":\"") + 11;
                                int e = err.indexOf("\"", s);
                                if (s > 10 && e > s) msg = err.substring(s, e);
                            }
                        } catch (Exception ignored) {}
                    }
                    result.postValue(AuthRepository.Result.error("Lỗi HTTP " + response.code() + ": " + msg));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Cancel Order ───────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> cancelOrder(String orderId) {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        orderApi.cancelOrder(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful()) {
                    result.postValue(AuthRepository.Result.success(null));
                } else {
                    result.postValue(AuthRepository.Result.error("Không thể hủy đơn hàng"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối"));
            }
        });
        return result;
    }

    // ── Return Request ─────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> returnOrder(String orderId, String reason) {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        Map<String, String> body = new HashMap<>();
        body.put("reason", reason);

        orderApi.returnRequest(orderId, body).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful()) {
                    result.postValue(AuthRepository.Result.success(null));
                } else {
                    result.postValue(AuthRepository.Result.error("Không thể gửi yêu cầu trả hàng"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối"));
            }
        });
        return result;
    }

    // ── Payment URL ────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<String>> getVnpayUrl(String orderId) {
        MutableLiveData<AuthRepository.Result<String>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId);

        paymentApi.createVNPayUrl(body).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    String url = response.body().getData().get("paymentUrl");
                    result.postValue(AuthRepository.Result.success(url != null ? url : ""));
                } else {
                    result.postValue(AuthRepository.Result.error("Không tạo được liên kết VNPay"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối"));
            }
        });
        return result;
    }

    public LiveData<AuthRepository.Result<String>> getMomoUrl(String orderId) {
        MutableLiveData<AuthRepository.Result<String>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId);

        paymentApi.createMoMoUrl(body).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    String url = response.body().getData().get("payUrl");
                    result.postValue(AuthRepository.Result.success(url != null ? url : ""));
                } else {
                    result.postValue(AuthRepository.Result.error("Không tạo được liên kết MoMo"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối"));
            }
        });
        return result;
    }

    // ── Available Vouchers ─────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<List<Voucher>>> getAvailableVouchers() {
        MutableLiveData<AuthRepository.Result<List<Voucher>>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        orderApi.getAvailableVouchers().enqueue(new Callback<ApiResponse<List<Voucher>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Voucher>>> call, Response<ApiResponse<List<Voucher>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.error("Không thể tải voucher"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Voucher>>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối"));
            }
        });
        return result;
    }

    // ── Reviews ───────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> submitReview(
            String orderId, java.util.List<java.util.Map<String, Object>> reviews) {
        MutableLiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> result =
                new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        Map<String, Object> body = new HashMap<>();
        body.put("reviews", reviews);

        orderApi.submitReview(orderId, body).enqueue(new Callback<ApiResponse<vn.vuavuive.shared.data.dto.Review>>() {
            @Override
            public void onResponse(Call<ApiResponse<vn.vuavuive.shared.data.dto.Review>> call,
                                   Response<ApiResponse<vn.vuavuive.shared.data.dto.Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.error("Khong the gui danh gia"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<vn.vuavuive.shared.data.dto.Review>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Loi ket noi: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> getMyReview(String orderId) {
        MutableLiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> result =
                new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        orderApi.getMyReview(orderId).enqueue(new Callback<ApiResponse<vn.vuavuive.shared.data.dto.Review>>() {
            @Override
            public void onResponse(Call<ApiResponse<vn.vuavuive.shared.data.dto.Review>> call,
                                   Response<ApiResponse<vn.vuavuive.shared.data.dto.Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.error("Khong the tai danh gia"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<vn.vuavuive.shared.data.dto.Review>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Loi ket noi: " + t.getMessage()));
            }
        });
        return result;
    }
}
