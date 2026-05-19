package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.data.repository.ShipmentRepository;
import vn.vuavuive.shared.data.dto.Shipment;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class ShipmentViewModel extends ViewModel {

    private final ShipmentRepository shipmentRepository;

    @Inject
    public ShipmentViewModel(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    public LiveData<AuthRepository.Result<List<Shipment>>> getMyShipments() {
        return shipmentRepository.getMyShipments();
    }

    public LiveData<AuthRepository.Result<Shipment>> getShipmentDetail(String shipmentId) {
        return shipmentRepository.getShipmentDetail(shipmentId);
    }
}
