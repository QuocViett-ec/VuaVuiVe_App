package vn.vuavuive.customer.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import vn.vuavuive.shared.data.local.CartDao;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.SessionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseCartRepository {

    private static final String TAG = "FirebaseCartRepository";
    private final CartDao cartDao;
    private final SessionManager sessionManager;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler syncHandler = new Handler(Looper.getMainLooper());
    private Runnable syncRunnable;

    @Inject
    public FirebaseCartRepository(CartDao cartDao, SessionManager sessionManager) {
        this.cartDao = cartDao;
        this.sessionManager = sessionManager;
    }

    private boolean isUserLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private String getCurrentUserUid() {
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private String getCurrentIsoString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    // ── Local Room Database Observers ───────────────────────────────────────
    public LiveData<List<CartItemEntity>> getCartItems() {
        return cartDao.getCartItems();
    }

    public LiveData<List<CartItemEntity>> getSavedItems() {
        return cartDao.getSavedItems();
    }

    public LiveData<Integer> getCartCount() {
        return cartDao.getCartCount();
    }

    // ── Mutation methods (Local Room database first, then Firebase sync) ────
    public void addItem(CartItemEntity item) {
        executor.execute(() -> {
            CartItemEntity existing = cartDao.getCartItem(item.getProductId());
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                cartDao.upsert(existing);
            } else {
                cartDao.upsert(item);
            }
            if (isUserLoggedIn()) {
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
            if (isUserLoggedIn()) {
                scheduleSync();
            }
        });
    }

    public void removeItem(String productId) {
        executor.execute(() -> {
            cartDao.delete(productId);
            if (isUserLoggedIn()) {
                scheduleSync();
            }
        });
    }

    public void saveForLater(String productId) {
        executor.execute(() -> {
            cartDao.setSavedForLater(productId, true);
            if (isUserLoggedIn()) {
                scheduleSync();
            }
        });
    }

    public void moveToCart(String productId) {
        executor.execute(() -> {
            cartDao.setSavedForLater(productId, false);
            if (isUserLoggedIn()) {
                scheduleSync();
            }
        });
    }

    public void clearCart() {
        executor.execute(() -> {
            cartDao.deleteAll();
            if (isUserLoggedIn()) {
                scheduleSync();
            }
        });
    }

    // ── Debounced sync to Firebase ──────────────────────────────────────────
    private void scheduleSync() {
        if (syncRunnable != null) {
            syncHandler.removeCallbacks(syncRunnable);
        }
        syncRunnable = this::pushToFirebase;
        syncHandler.postDelayed(syncRunnable, 500);
    }

    private void pushToFirebase() {
        String uid = getCurrentUserUid();
        if (uid == null) return;

        executor.execute(() -> {
            try {
                List<CartItemEntity> localItems = cartDao.getAllSync();
                Map<String, Object> itemsMap = new HashMap<>();
                Map<String, Object> savedMap = new HashMap<>();
                String now = getCurrentIsoString();

                if (localItems != null) {
                    for (CartItemEntity e : localItems) {
                        Map<String, Object> payload = serializeEntity(e, now);
                        if (e.isSavedForLater()) {
                            savedMap.put(e.getProductId(), payload);
                        } else {
                            itemsMap.put(e.getProductId(), payload);
                        }
                    }
                }

                DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("carts").child(uid);
                if (itemsMap.isEmpty() && savedMap.isEmpty()) {
                    cartRef.setValue(null);
                } else {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("updated_at", now);
                    updates.put("items", itemsMap.isEmpty() ? null : itemsMap);
                    updates.put("saved_for_later", savedMap.isEmpty() ? null : savedMap);
                    cartRef.updateChildren(updates);
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi push lên Firebase: " + e.getMessage());
            }
        });
    }

    // ── Sync from Firebase to Room ─────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> syncFromServer() {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        String uid = getCurrentUserUid();
        if (uid == null) {
            result.postValue(AuthRepository.Result.error("Chưa đăng nhập"));
            return result;
        }

        FirebaseDatabase.getInstance().getReference()
                .child("carts").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        executor.execute(() -> {
                            try {
                                cartDao.deleteAll();
                                DataSnapshot itemsSnap = snapshot.child("items");
                                if (itemsSnap.exists()) {
                                    for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                                        CartItemEntity entity = mapSnapshotToEntity(itemSnap, false);
                                        cartDao.upsert(entity);
                                    }
                                }

                                DataSnapshot savedSnap = snapshot.child("saved_for_later");
                                if (savedSnap.exists()) {
                                    for (DataSnapshot itemSnap : savedSnap.getChildren()) {
                                        CartItemEntity entity = mapSnapshotToEntity(itemSnap, true);
                                        cartDao.upsert(entity);
                                    }
                                }

                                result.postValue(AuthRepository.Result.success(null));
                            } catch (Exception e) {
                                result.postValue(AuthRepository.Result.error("Lỗi ghi dữ liệu vào Room: " + e.getMessage()));
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        result.postValue(AuthRepository.Result.error("Lỗi đọc Firebase: " + error.getMessage()));
                    }
                });

        return result;
    }

    // ── Merge Local & Firebase Cart ─────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> mergeWithServer() {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        String uid = getCurrentUserUid();
        if (uid == null) {
            result.postValue(AuthRepository.Result.error("Chưa đăng nhập"));
            return result;
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot rootSnapshot) {
                executor.execute(() -> {
                    try {
                        DataSnapshot cartSnapshot = rootSnapshot.child("carts").child(uid);
                        DataSnapshot productsSnapshot = rootSnapshot.child("products");

                        // Map of product_id -> latest stock_quantity from products node
                        Map<String, Integer> productStockMap = new HashMap<>();
                        if (productsSnapshot.exists()) {
                            for (DataSnapshot pSnap : productsSnapshot.getChildren()) {
                                String pid = pSnap.child("id").getValue(String.class);
                                Integer stock = pSnap.child("stock_quantity").getValue(Integer.class);
                                if (pid != null && stock != null) {
                                    productStockMap.put(pid, stock);
                                }
                            }
                        }

                        // Load remote cart items and savedItems
                        Map<String, CartItemEntity> mergedItems = new HashMap<>();
                        Map<String, CartItemEntity> mergedSaved = new HashMap<>();

                        DataSnapshot remoteItems = cartSnapshot.child("items");
                        if (remoteItems.exists()) {
                            for (DataSnapshot s : remoteItems.getChildren()) {
                                CartItemEntity entity = mapSnapshotToEntity(s, false);
                                if (entity.getProductId() != null) {
                                    mergedItems.put(entity.getProductId(), entity);
                                }
                            }
                        }

                        DataSnapshot remoteSaved = cartSnapshot.child("saved_for_later");
                        if (remoteSaved.exists()) {
                            for (DataSnapshot s : remoteSaved.getChildren()) {
                                CartItemEntity entity = mapSnapshotToEntity(s, true);
                                if (entity.getProductId() != null) {
                                    mergedSaved.put(entity.getProductId(), entity);
                                }
                            }
                        }

                        // Load local Room cart
                        List<CartItemEntity> localItems = cartDao.getAllSync();
                        if (localItems != null) {
                            for (CartItemEntity local : localItems) {
                                String pid = local.getProductId();
                                int latestStock = productStockMap.containsKey(pid) ? productStockMap.get(pid) : local.getProductStock();
                                local.setProductStock(latestStock);

                                if (local.isSavedForLater()) {
                                    if (mergedSaved.containsKey(pid)) {
                                        CartItemEntity remote = mergedSaved.get(pid);
                                        int mergedQty = Math.min(local.getQuantity() + remote.getQuantity(), latestStock);
                                        remote.setQuantity(mergedQty);
                                        remote.setProductStock(latestStock);
                                    } else {
                                        int qty = Math.min(local.getQuantity(), latestStock);
                                        local.setQuantity(qty);
                                        mergedSaved.put(pid, local);
                                    }
                                } else {
                                    if (mergedItems.containsKey(pid)) {
                                        CartItemEntity remote = mergedItems.get(pid);
                                        int mergedQty = Math.min(local.getQuantity() + remote.getQuantity(), latestStock);
                                        remote.setQuantity(mergedQty);
                                        remote.setProductStock(latestStock);
                                    } else {
                                        int qty = Math.min(local.getQuantity(), latestStock);
                                        local.setQuantity(qty);
                                        mergedItems.put(pid, local);
                                    }
                                }
                            }
                        }

                        // Write merged cart back to Firebase Realtime Database
                        String now = getCurrentIsoString();
                        Map<String, Object> cartUpdate = new HashMap<>();
                        cartUpdate.put("updated_at", now);

                        Map<String, Object> itemsUpload = new HashMap<>();
                        for (CartItemEntity e : mergedItems.values()) {
                            itemsUpload.put(e.getProductId(), serializeEntity(e, now));
                        }
                        cartUpdate.put("items", itemsUpload.isEmpty() ? null : itemsUpload);

                        Map<String, Object> savedUpload = new HashMap<>();
                        for (CartItemEntity e : mergedSaved.values()) {
                            savedUpload.put(e.getProductId(), serializeEntity(e, now));
                        }
                        cartUpdate.put("saved_for_later", savedUpload.isEmpty() ? null : savedUpload);

                        rootRef.child("carts").child(uid).setValue(cartUpdate).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                syncFromServer();
                                result.postValue(AuthRepository.Result.success(null));
                            } else {
                                String err = task.getException() != null ? task.getException().getMessage() : "Lỗi ghi Firebase";
                                result.postValue(AuthRepository.Result.error(err));
                            }
                        });

                    } catch (Exception e) {
                        result.postValue(AuthRepository.Result.error("Lỗi merge giỏ hàng: " + e.getMessage()));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.error("Lỗi đọc Firebase: " + error.getMessage()));
            }
        });

        return result;
    }

    // ── Mapping & Serialization Helpers ─────────────────────────────────────
    private CartItemEntity mapSnapshotToEntity(DataSnapshot s, boolean savedForLater) {
        CartItemEntity e = new CartItemEntity();
        e.setProductId(s.child("product_id").getValue(String.class));
        Integer qty = s.child("quantity").getValue(Integer.class);
        e.setQuantity(qty != null ? qty : 1);
        e.setSavedForLater(savedForLater);
        e.setAddedAt(System.currentTimeMillis());
        e.setProductName(s.child("product_name").getValue(String.class));
        Double price = s.child("unit_price").getValue(Double.class);
        e.setProductPrice(price != null ? price : 0.0);
        e.setProductImageUrl(s.child("image_url").getValue(String.class));
        e.setProductUnit(s.child("unit").getValue(String.class));
        Integer stock = s.child("stock_quantity").getValue(Integer.class);
        e.setProductStock(stock != null ? stock : 999);
        return e;
    }

    private Map<String, Object> serializeEntity(CartItemEntity e, String updatedAt) {
        Map<String, Object> m = new HashMap<>();
        m.put("product_id", e.getProductId());
        m.put("quantity", e.getQuantity());
        m.put("product_name", e.getProductName());
        m.put("unit_price", e.getProductPrice());
        m.put("image_url", e.getProductImageUrl());
        m.put("unit", e.getProductUnit());
        m.put("stock_quantity", e.getProductStock());
        m.put("subtotal", e.getLineTotal());
        m.put("updated_at", updatedAt);
        return m;
    }
}
