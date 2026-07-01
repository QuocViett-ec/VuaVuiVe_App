package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.data.repository.OrderRepository;
import vn.vuavuive.shared.data.dto.CreateMomoPaymentResponse;
import vn.vuavuive.shared.data.dto.CreateZaloPayPaymentResponse;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.PaymentStatusResponse;
import vn.vuavuive.shared.data.dto.Voucher;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class OrderViewModel extends ViewModel {

    private final vn.vuavuive.customer.data.repository.FirebaseOrderRepository firebaseOrderRepository;
    private final OrderRepository backendOrderRepository;

    // Selected voucher
    private final MutableLiveData<Voucher> selectedVoucher = new MutableLiveData<>();

    @Inject
    public OrderViewModel(vn.vuavuive.customer.data.repository.FirebaseOrderRepository firebaseOrderRepository,
                          OrderRepository backendOrderRepository) {
        this.firebaseOrderRepository = firebaseOrderRepository;
        this.backendOrderRepository = backendOrderRepository;
    }

    public LiveData<AuthRepository.Result<List<Order>>> getOrders(String status, int page) {
        return backendOrderRepository.getOrders(status, page, 20);
    }

    public LiveData<AuthRepository.Result<Order>> getOrderDetail(String orderId) {
        return backendOrderRepository.getOrderDetail(orderId);
    }

    public LiveData<AuthRepository.Result<Order>> createOrder(CreateOrderRequest request) {
        return backendOrderRepository.createOrder(request);
    }

    public LiveData<AuthRepository.Result<Void>> cancelOrder(String orderId) {
        return backendOrderRepository.cancelOrder(orderId);
    }

    public LiveData<AuthRepository.Result<Void>> returnOrder(String orderId, String reason) {
        return firebaseOrderRepository.returnOrder(orderId, reason);
    }

    public LiveData<AuthRepository.Result<CreateMomoPaymentResponse>> createMomoPayment(
            String orderId, double amount, String userId) {
        return backendOrderRepository.createMomoPayment(orderId, amount, userId);
    }

    public LiveData<AuthRepository.Result<CreateZaloPayPaymentResponse>> createZaloPayPayment(
            String orderId, double amount, String description) {
        return backendOrderRepository.createZaloPayPayment(orderId, amount, description);
    }

    public LiveData<AuthRepository.Result<PaymentStatusResponse>> getPaymentStatus(String orderId) {
        return backendOrderRepository.getPaymentStatus(orderId);
    }

    public LiveData<AuthRepository.Result<PaymentStatusResponse>> mockMomoSuccess(String orderId) {
        return backendOrderRepository.mockMomoSuccess(orderId);
    }

    public LiveData<AuthRepository.Result<PaymentStatusResponse>> mockZaloPaySuccess(String orderId) {
        return backendOrderRepository.mockZaloPaySuccess(orderId);
    }

    public LiveData<AuthRepository.Result<List<Voucher>>> getAvailableVouchers() {
        return backendOrderRepository.getAvailableVouchers();
    }

    public LiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> submitReview(
            String orderId, java.util.List<java.util.Map<String, Object>> reviews) {
        return backendOrderRepository.submitReview(orderId, reviews);
    }

    public LiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> getMyReview(String orderId) {
        return backendOrderRepository.getMyReview(orderId);
    }

    public MutableLiveData<Voucher> getSelectedVoucher() { return selectedVoucher; }
    public void setSelectedVoucher(Voucher voucher) { selectedVoucher.setValue(voucher); }
}
