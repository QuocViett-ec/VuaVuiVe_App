package vn.vuavuive.customer.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.ShipmentApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Shipment;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ShipmentRepository {

    private final ShipmentApi shipmentApi;

    @Inject
    public ShipmentRepository(ShipmentApi shipmentApi) {
        this.shipmentApi = shipmentApi;
    }

    public LiveData<AuthRepository.Result<List<Shipment>>> getMyShipments() {
        MutableLiveData<AuthRepository.Result<List<Shipment>>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        shipmentApi.getMyShipments().enqueue(new Callback<ApiResponse<List<Shipment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Shipment>>> call,
                                   Response<ApiResponse<List<Shipment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.error("Khong the tai danh sach van chuyen"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Shipment>>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Loi ket noi: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<AuthRepository.Result<Shipment>> getShipmentDetail(String shipmentId) {
        MutableLiveData<AuthRepository.Result<Shipment>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        shipmentApi.getShipment(shipmentId).enqueue(new Callback<ApiResponse<Shipment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Shipment>> call,
                                   Response<ApiResponse<Shipment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    result.postValue(AuthRepository.Result.error("Khong the tai chi tiet van chuyen"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Shipment>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error("Loi ket noi: " + t.getMessage()));
            }
        });
        return result;
    }
}
