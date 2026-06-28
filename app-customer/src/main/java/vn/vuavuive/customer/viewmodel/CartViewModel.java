package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import vn.vuavuive.customer.data.repository.FirebaseCartRepository;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.shared.data.local.CartItemEntity;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class CartViewModel extends ViewModel {

    private final FirebaseCartRepository cartRepository;

    @Inject
    public CartViewModel(FirebaseCartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public LiveData<List<CartItemEntity>> getCartItems() {
        return cartRepository.getCartItems();
    }

    public LiveData<List<CartItemEntity>> getSavedItems() {
        return cartRepository.getSavedItems();
    }

    public LiveData<Integer> getCartCount() {
        return cartRepository.getCartCount();
    }

    public void addItem(CartItemEntity item) {
        cartRepository.addItem(item);
    }

    public void updateQuantity(String productId, int quantity) {
        cartRepository.updateQuantity(productId, quantity);
    }

    public void removeItem(String productId) {
        cartRepository.removeItem(productId);
    }

    public void saveForLater(String productId) {
        cartRepository.saveForLater(productId);
    }

    public void moveToCart(String productId) {
        cartRepository.moveToCart(productId);
    }

    public void clearCart() {
        cartRepository.clearCart();
    }

    public void removeLegacyMockItems() {
        cartRepository.removeLegacyMockItems();
    }

    /** Đồng bộ cart từ Firebase (gọi sau khi login) */
    public void onUserLoggedIn() {
        cartRepository.onUserLoggedIn();
    }

    /** Xóa cart local (gọi sau khi logout) */
    public void onUserLoggedOut() {
        cartRepository.onUserLoggedOut();
    }

    /** Synchronous snapshot — MUST be called from a background thread */
    public List<CartItemEntity> getCartItemsSync() {
        return cartRepository.getCartItemsSync();
    }

    public LiveData<AuthRepository.Result<Void>> syncFromServer() {
        return cartRepository.syncFromServer();
    }

    public LiveData<AuthRepository.Result<Void>> mergeWithServer() {
        return cartRepository.mergeWithServer();
    }
}
