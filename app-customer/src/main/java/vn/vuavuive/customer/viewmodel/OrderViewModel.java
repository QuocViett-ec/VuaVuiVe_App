package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.data.repository.OrderRepository;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.Voucher;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class OrderViewModel extends ViewModel {

    private final OrderRepository orderRepository;

    // Selected voucher
    private final MutableLiveData<Voucher> selectedVoucher = new MutableLiveData<>();

    @Inject
    public OrderViewModel(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public LiveData<AuthRepository.Result<List<Order>>> getOrders(String status, int page) {
        return orderRepository.getOrders(status, page, 20);
    }

    public LiveData<AuthRepository.Result<Order>> getOrderDetail(String orderId) {
        return orderRepository.getOrderDetail(orderId);
    }

    public LiveData<AuthRepository.Result<Order>> createOrder(CreateOrderRequest request) {
        return orderRepository.createOrder(request);
    }

    public LiveData<AuthRepository.Result<Void>> cancelOrder(String orderId) {
        return orderRepository.cancelOrder(orderId);
    }

    public LiveData<AuthRepository.Result<Void>> returnOrder(String orderId, String reason) {
        return orderRepository.returnOrder(orderId, reason);
    }

    public LiveData<AuthRepository.Result<String>> getVnpayUrl(String orderId) {
        return orderRepository.getVnpayUrl(orderId);
    }

    public LiveData<AuthRepository.Result<String>> getMomoUrl(String orderId) {
        return orderRepository.getMomoUrl(orderId);
    }

    public LiveData<AuthRepository.Result<List<Voucher>>> getAvailableVouchers() {
        return orderRepository.getAvailableVouchers();
    }

    public LiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> submitReview(
            String orderId, java.util.List<java.util.Map<String, Object>> reviews) {
        return orderRepository.submitReview(orderId, reviews);
    }

    public LiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> getMyReview(String orderId) {
        return orderRepository.getMyReview(orderId);
    }

    public MutableLiveData<Voucher> getSelectedVoucher() { return selectedVoucher; }
    public void setSelectedVoucher(Voucher voucher) { selectedVoucher.setValue(voucher); }
}
