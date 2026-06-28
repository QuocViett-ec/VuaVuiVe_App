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
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.SessionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * FirebaseCartRepository — Quản lý giỏ hàng 100% trên Firebase Realtime Database.
 * Không còn sử dụng Room/SQLite. Dữ liệu được lưu in-memory (Map) và sync với Firebase.
 */
@Singleton
public class FirebaseCartRepository {

    private static final String TAG = "FirebaseCartRepository";

    private final SessionManager sessionManager;
    private final Handler syncHandler = new Handler(Looper.getMainLooper());
    private Runnable syncRunnable;

    // ── In-memory store ─────────────────────────────────────────────────────
    /** Map productId -> CartItemEntity (active cart items) */
    private final Map<String, CartItemEntity> cartMap = new LinkedHashMap<>();
    /** Map productId -> CartItemEntity (saved for later) */
    private final Map<String, CartItemEntity> savedMap = new LinkedHashMap<>();

    // ── LiveData ─────────────────────────────────────────────────────────────
    private final MutableLiveData<List<CartItemEntity>> cartItemsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<CartItemEntity>> savedItemsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> cartCountLiveData = new MutableLiveData<>(0);

    // ── Firebase realtime listener ────────────────────────────────────────
    private ValueEventListener firebaseListener;
    private String listenedUid;

    @Inject
    public FirebaseCartRepository(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        attachFirebaseListener();
    }

    // ── Firebase Auth Helpers ────────────────────────────────────────────────
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

    // ── Attach realtime listener to Firebase ─────────────────────────────────
    private void attachFirebaseListener() {
        String uid = getCurrentUserUid();
        if (uid == null) return;
        if (uid.equals(listenedUid) && firebaseListener != null) return;

        // Detach previous listener if any
        detachFirebaseListener();
        listenedUid = uid;

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(uid);
        firebaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartMap.clear();
                savedMap.clear();

                DataSnapshot itemsSnap = snapshot.child("items");
                if (itemsSnap.exists()) {
                    for (DataSnapshot s : itemsSnap.getChildren()) {
                        CartItemEntity e = mapSnapshotToEntity(s, false);
                        if (e.getProductId() != null && !e.getProductId().isEmpty()) {
                            cartMap.put(e.getProductId(), e);
                        }
                    }
                }

                DataSnapshot savedSnap = snapshot.child("saved_for_later");
                if (savedSnap.exists()) {
                    for (DataSnapshot s : savedSnap.getChildren()) {
                        CartItemEntity e = mapSnapshotToEntity(s, true);
                        if (e.getProductId() != null && !e.getProductId().isEmpty()) {
                            savedMap.put(e.getProductId(), e);
                        }
                    }
                }

                notifyObservers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase cart listener cancelled: " + error.getMessage());
            }
        };
        cartRef.addValueEventListener(firebaseListener);
    }

    private void detachFirebaseListener() {
        if (firebaseListener != null && listenedUid != null) {
            FirebaseDatabase.getInstance().getReference("carts")
                    .child(listenedUid).removeEventListener(firebaseListener);
            firebaseListener = null;
            listenedUid = null;
        }
    }

    /** Call this after login to attach listener for new user */
    public void onUserLoggedIn() {
        cartMap.clear();
        savedMap.clear();
        notifyObservers();
        attachFirebaseListener();
    }

    /** Call this after logout to clear everything */
    public void onUserLoggedOut() {
        detachFirebaseListener();
        cartMap.clear();
        savedMap.clear();
        notifyObservers();
    }

    // ── Push current in-memory state to Firebase ────────────────────────────
    private void notifyObservers() {
        cartItemsLiveData.postValue(new ArrayList<>(cartMap.values()));
        savedItemsLiveData.postValue(new ArrayList<>(savedMap.values()));
        cartCountLiveData.postValue(cartMap.size());
    }

    private void scheduleSync() {
        if (syncRunnable != null) syncHandler.removeCallbacks(syncRunnable);
        syncRunnable = this::pushToFirebase;
        syncHandler.postDelayed(syncRunnable, 300);
    }

    private void pushToFirebase() {
        String uid = getCurrentUserUid();
        if (uid == null) return;

        String now = getCurrentIsoString();
        Map<String, Object> itemsUpload = new HashMap<>();
        for (CartItemEntity e : cartMap.values()) {
            itemsUpload.put(e.getProductId(), serializeEntity(e, now));
        }

        Map<String, Object> savedUpload = new HashMap<>();
        for (CartItemEntity e : savedMap.values()) {
            savedUpload.put(e.getProductId(), serializeEntity(e, now));
        }

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(uid);
        if (itemsUpload.isEmpty() && savedUpload.isEmpty()) {
            cartRef.setValue(null);
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put("updated_at", now);
            updates.put("items", itemsUpload.isEmpty() ? null : itemsUpload);
            updates.put("saved_for_later", savedUpload.isEmpty() ? null : savedUpload);
            cartRef.updateChildren(updates);
        }
    }

    // ── LiveData Observers ───────────────────────────────────────────────────
    public LiveData<List<CartItemEntity>> getCartItems() {
        attachFirebaseListener(); // re-attach if needed
        return cartItemsLiveData;
    }

    public LiveData<List<CartItemEntity>> getSavedItems() {
        return savedItemsLiveData;
    }

    public LiveData<Integer> getCartCount() {
        return cartCountLiveData;
    }

    // ── Mutation Methods ─────────────────────────────────────────────────────
    public void addItem(CartItemEntity item) {
        if (item == null || item.getProductId() == null || item.getProductId().isEmpty()) return;

        String pid = item.getProductId();
        if (cartMap.containsKey(pid)) {
            CartItemEntity existing = cartMap.get(pid);
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            cartMap.put(pid, item);
        }
        notifyObservers();
        if (isUserLoggedIn()) scheduleSync();
    }

    public void updateQuantity(String productId, int quantity) {
        if (quantity <= 0) {
            cartMap.remove(productId);
        } else {
            CartItemEntity e = cartMap.get(productId);
            if (e != null) e.setQuantity(quantity);
        }
        notifyObservers();
        if (isUserLoggedIn()) scheduleSync();
    }

    public void removeItem(String productId) {
        cartMap.remove(productId);
        savedMap.remove(productId);
        notifyObservers();
        if (isUserLoggedIn()) scheduleSync();
    }

    public void saveForLater(String productId) {
        CartItemEntity e = cartMap.remove(productId);
        if (e != null) {
            e.setSavedForLater(true);
            savedMap.put(productId, e);
            notifyObservers();
            if (isUserLoggedIn()) scheduleSync();
        }
    }

    public void moveToCart(String productId) {
        CartItemEntity e = savedMap.remove(productId);
        if (e != null) {
            e.setSavedForLater(false);
            cartMap.put(productId, e);
            notifyObservers();
            if (isUserLoggedIn()) scheduleSync();
        }
    }

    public void clearCart() {
        cartMap.clear();
        notifyObservers();
        if (isUserLoggedIn()) scheduleSync();
    }

    public void removeLegacyMockItems() {
        // Remove any items whose productId starts with legacy prefixes
        cartMap.entrySet().removeIf(entry ->
                entry.getKey().startsWith("prod-") || entry.getKey().startsWith("mock_"));
        savedMap.entrySet().removeIf(entry ->
                entry.getKey().startsWith("prod-") || entry.getKey().startsWith("mock_"));
        notifyObservers();
        if (isUserLoggedIn()) scheduleSync();
    }

    /** Synchronous snapshot — safe to call from any thread */
    public List<CartItemEntity> getCartItemsSync() {
        return new ArrayList<>(cartMap.values());
    }

    // ── Sync from Firebase (one-shot pull) ──────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> syncFromServer() {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        String uid = getCurrentUserUid();
        if (uid == null) {
            result.postValue(AuthRepository.Result.error("Chưa đăng nhập"));
            return result;
        }

        FirebaseDatabase.getInstance().getReference("carts").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cartMap.clear();
                        savedMap.clear();

                        DataSnapshot itemsSnap = snapshot.child("items");
                        if (itemsSnap.exists()) {
                            for (DataSnapshot s : itemsSnap.getChildren()) {
                                CartItemEntity e = mapSnapshotToEntity(s, false);
                                if (e.getProductId() != null) cartMap.put(e.getProductId(), e);
                            }
                        }

                        DataSnapshot savedSnap = snapshot.child("saved_for_later");
                        if (savedSnap.exists()) {
                            for (DataSnapshot s : savedSnap.getChildren()) {
                                CartItemEntity e = mapSnapshotToEntity(s, true);
                                if (e.getProductId() != null) savedMap.put(e.getProductId(), e);
                            }
                        }

                        notifyObservers();
                        result.postValue(AuthRepository.Result.success(null));
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

        // Save current local state before pulling remote
        Map<String, CartItemEntity> localCart = new HashMap<>(cartMap);
        Map<String, CartItemEntity> localSaved = new HashMap<>(savedMap);

        FirebaseDatabase.getInstance().getReference("carts").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Merge remote into local
                        DataSnapshot remoteItems = snapshot.child("items");
                        if (remoteItems.exists()) {
                            for (DataSnapshot s : remoteItems.getChildren()) {
                                CartItemEntity remote = mapSnapshotToEntity(s, false);
                                if (remote.getProductId() == null) continue;
                                String pid = remote.getProductId();
                                if (localCart.containsKey(pid)) {
                                    // Combine quantities, cap at stock
                                    CartItemEntity local = localCart.get(pid);
                                    int merged = local.getQuantity() + remote.getQuantity();
                                    if (remote.getProductStock() > 0) {
                                        merged = Math.min(merged, remote.getProductStock());
                                    }
                                    local.setQuantity(merged);
                                } else {
                                    localCart.put(pid, remote);
                                }
                            }
                        }

                        DataSnapshot remoteSaved = snapshot.child("saved_for_later");
                        if (remoteSaved.exists()) {
                            for (DataSnapshot s : remoteSaved.getChildren()) {
                                CartItemEntity remote = mapSnapshotToEntity(s, true);
                                if (remote.getProductId() == null) continue;
                                String pid = remote.getProductId();
                                if (!localSaved.containsKey(pid)) {
                                    localSaved.put(pid, remote);
                                }
                            }
                        }

                        // Update in-memory
                        cartMap.clear();
                        cartMap.putAll(localCart);
                        savedMap.clear();
                        savedMap.putAll(localSaved);
                        notifyObservers();

                        // Push merged state to Firebase
                        pushToFirebase();
                        result.postValue(AuthRepository.Result.success(null));
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
