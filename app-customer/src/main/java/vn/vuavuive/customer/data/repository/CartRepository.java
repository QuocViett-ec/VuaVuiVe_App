package vn.vuavuive.customer.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.CartApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Cart;
import vn.vuavuive.shared.data.dto.CartItem;
import vn.vuavuive.shared.data.local.CartDao;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.SessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CartRepository {

    private final CartApi cartApi;
    private final CartDao cartDao;
    private final SessionManager sessionManager;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public CartRepository(CartApi cartApi, CartDao cartDao, SessionManager sessionManager) {
        this.cartApi = cartApi;
        this.cartDao = cartDao;
        this.sessionManager = sessionManager;
    }

    // ── Local observers ────────────────────────────────────────────────────
    public LiveData<List<CartItemEntity>> getCartItems() {
        return cartDao.getCartItems();
    }

    public LiveData<List<CartItemEntity>> getSavedItems() {
        return cartDao.getSavedItems();
    }

    public LiveData<Integer> getCartCount() {
        return cartDao.getCartCount();
    }

    // ── Add / Update item ──────────────────────────────────────────────────
    public void addItem(CartItemEntity item) {
        executor.execute(() -> {
            CartItemEntity existing = cartDao.getCartItem(item.getProductId());
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                cartDao.upsert(existing);
            } else {
                cartDao.upsert(item);
            }
            if (sessionManager.isLoggedIn()) {
                scheduleSync();
            }
        });
    }

    public void updateQuantity(String productId, int quantity) {
        executor.execute(() -> {
            if (quantity <= 0) {
                cartDao.delete(productId);
            } else {
                cartDao.updateQuantity(productId, quantity);
            }
            if (sessionManager.isLoggedIn()) {
                scheduleSync();
            }
        });
    }

    public void removeItem(String productId) {
        executor.execute(() -> {
            cartDao.delete(productId);
            if (sessionManager.isLoggedIn()) {
                scheduleSync();
            }
        });
    }

    public void saveForLater(String productId) {
        executor.execute(() -> {
            cartDao.setSavedForLater(productId, true);
        });
    }

    public void moveToCart(String productId) {
        executor.execute(() -> {
            cartDao.setSavedForLater(productId, false);
        });
    }

    public void clearCart() {
        executor.execute(() -> cartDao.deleteAll());
    }

    // ── Sync with server ───────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> syncFromServer() {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();

        cartApi.getCart().enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Cart cart = response.body().getData();
                    // Update local Room from server cart
                    executor.execute(() -> {
                        cartDao.deleteAll();
                        if (cart.getItems() != null) {
                            for (CartItem item : cart.getItems()) {
                                CartItemEntity entity = CartItemEntity.fromCartItem(item, false);
                                cartDao.upsert(entity);
                            }
                        }
                        if (cart.getSavedForLater() != null) {
                            for (CartItem item : cart.getSavedForLater()) {
                                CartItemEntity entity = CartItemEntity.fromCartItem(item, true);
                                cartDao.upsert(entity);
                            }
                        }
                    });
                    result.postValue(AuthRepository.Result.success(null));
                } else {
                    result.postValue(AuthRepository.Result.error("Không thể tải giỏ hàng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                result.postValue(AuthRepository.Result.error(t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<AuthRepository.Result<Void>> mergeWithServer() {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();

        executor.execute(() -> {
            List<CartItemEntity> localItems = cartDao.getAllSync();
            List<Map<String, Object>> items = new ArrayList<>();
            List<Map<String, Object>> saved = new ArrayList<>();

            for (CartItemEntity e : localItems) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("productId", e.getProductId());
                payload.put("quantity", e.getQuantity());
                if (e.isSavedForLater()) saved.add(payload);
                else items.add(payload);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("items", items);
            body.put("savedForLater", saved);

            cartApi.mergeCart(body).enqueue(new Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                    if (response.isSuccessful()) {
                        result.postValue(AuthRepository.Result.success(null));
                        // Reload from server
                        syncFromServer();
                    } else {
                        result.postValue(AuthRepository.Result.error("Merge thất bại"));
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                    result.postValue(AuthRepository.Result.error(t.getMessage()));
                }
            });
        });
        return result;
    }

    // ── Debounce sync ──────────────────────────────────────────────────────
    private android.os.Handler syncHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable syncRunnable;

    private void scheduleSync() {
        if (syncHandler != null && syncRunnable != null) {
            syncHandler.removeCallbacks(syncRunnable);
        }
        syncRunnable = this::pushToServer;
        syncHandler.postDelayed(syncRunnable, 500);
    }

    private void pushToServer() {
        executor.execute(() -> {
            List<CartItemEntity> localItems = cartDao.getAllSync();
            if (localItems == null || localItems.isEmpty()) return;

            List<Map<String, Object>> items = new ArrayList<>();
            List<Map<String, Object>> saved = new ArrayList<>();

            for (CartItemEntity e : localItems) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("productId", e.getProductId());
                payload.put("quantity", e.getQuantity());
                if (e.isSavedForLater()) saved.add(payload);
                else items.add(payload);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("items", items);
            body.put("savedForLater", saved);

            cartApi.syncCart(body).enqueue(new Callback<ApiResponse<Cart>>() {
                @Override public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> r) {}
                @Override public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {}
            });
        });
    }
}
