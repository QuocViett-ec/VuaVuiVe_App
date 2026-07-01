package vn.vuavuive.shipper.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.SessionManager;

/**
 * FirebaseShipperRepository — Repository duy nhất cho app-shipper.
 *
 * Chức năng:
 * 1. Đăng nhập Firebase Auth, đọc profile từ /users/{uid}, kiểm tra role == SHIPPER.
 * 2. Lấy danh sách đơn hàng từ /orders WHERE shipperId == currentUid.
 * 3. Cập nhật trạng thái đơn hàng (IN_TRANSIT / DELIVERED / FAILED).
 * 4. Cập nhật trạng thái online/offline của shipper tại /users/{uid}/onlineStatus.
 */
@Singleton
public class FirebaseShipperRepository {

    private static final String TAG = "FirebaseShipperRepo";

    private final FirebaseAuth auth;
    private final DatabaseReference dbRef;
    private final SessionManager sessionManager;

    @Inject
    public FirebaseShipperRepository(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.auth = FirebaseAuth.getInstance();
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Đăng nhập bằng Firebase Auth.
     * Sau khi login thành công, đọc /users/{uid} để lấy profile và kiểm tra role SHIPPER.
     */
    public LiveData<Result<User>> login(String email, String password) {
        MutableLiveData<Result<User>> out = new MutableLiveData<>();
        out.setValue(Result.loading());

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        out.setValue(Result.error("Đăng nhập thất bại"));
                        return;
                    }
                    // Đọc profile từ Realtime Database
                    dbRef.child("users").child(firebaseUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        auth.signOut();
                                        out.setValue(Result.error("Không tìm thấy tài khoản"));
                                        return;
                                    }

                                    String role = snapshot.child("role").getValue(String.class);
                                    if (!"SHIPPER".equalsIgnoreCase(role)) {
                                        auth.signOut();
                                        out.setValue(Result.error("Tài khoản không có quyền Shipper"));
                                        return;
                                    }

                                    // Xây dựng User object
                                    User user = new User();
                                    user.setId(firebaseUser.getUid());
                                    user.setEmail(firebaseUser.getEmail());

                                    String name = snapshot.child("name").getValue(String.class);
                                    String phone = snapshot.child("phone").getValue(String.class);
                                    user.setName(name != null ? name : firebaseUser.getEmail());
                                    user.setPhone(phone);
                                    user.setRole("SHIPPER");

                                    // Lưu session
                                    sessionManager.saveUser(user);
                                    // Dùng Firebase UID token làm access token
                                    firebaseUser.getIdToken(false).addOnSuccessListener(tokenResult -> {
                                        String idToken = tokenResult.getToken();
                                        sessionManager.saveTokens(idToken, null);
                                        out.setValue(Result.success(user));
                                    }).addOnFailureListener(e -> {
                                        // Token fetch thất bại nhưng user vẫn ok
                                        out.setValue(Result.success(user));
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    auth.signOut();
                                    out.setValue(Result.error("Lỗi đọc dữ liệu: " + error.getMessage()));
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "login failed", e);
                    out.setValue(Result.error("Sai email hoặc mật khẩu"));
                });

        return out;
    }

    /**
     * Kiểm tra Firebase session còn hợp lệ.
     * FirebaseAuth tự refresh token nên chỉ cần check currentUser != null và role trong session.
     */
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null && sessionManager.isShipper();
    }

    /**
     * Đăng xuất Firebase Auth và xoá session local.
     */
    public void logout() {
        auth.signOut();
        sessionManager.clearSession();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ORDERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Lắng nghe real-time danh sách đơn hàng được gán cho shipper hiện tại.
     * Query: /orders WHERE shipperId == currentUid
     *
     * Firebase RTDB không hỗ trợ WHERE trực tiếp nên ta lấy toàn bộ /orders
     * và filter phía client theo shipperId.
     *
     * Lưu ý: Với quy mô thực tế, nên index shipperId hoặc dùng /shipper_orders/{uid}/orderId.
     */
    public LiveData<Result<List<Order>>> getMyOrders() {
        return new LiveData<Result<List<Order>>>() {
            private ValueEventListener listener;
            private DatabaseReference ref;

            @Override
            protected void onActive() {
                super.onActive();
                postValue(Result.loading());

                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser == null) {
                    postValue(Result.error("Chưa đăng nhập"));
                    return;
                }
                String uid = currentUser.getUid();

                ref = dbRef.child("orders");
                listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Order> orders = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Order order = mapSnapshotToOrder(child);
                            if (order != null && uid.equals(order.getShipperId())) {
                                orders.add(order);
                            }
                        }
                        // Sắp xếp mới nhất lên trước
                        Collections.sort(orders, (a, b) -> {
                            String ca = a.getCreatedAt() != null ? a.getCreatedAt() : "";
                            String cb = b.getCreatedAt() != null ? b.getCreatedAt() : "";
                            return cb.compareTo(ca);
                        });
                        postValue(Result.success(orders));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "orders query cancelled: " + error.getMessage());
                        postValue(Result.error("Không tải được danh sách đơn: " + error.getMessage()));
                    }
                };
                ref.addValueEventListener(listener);
            }

            @Override
            protected void onInactive() {
                super.onInactive();
                if (ref != null && listener != null) {
                    ref.removeEventListener(listener);
                    listener = null;
                    ref = null;
                }
            }
        };
    }

    /**
     * Lấy chi tiết một đơn hàng (one-shot, không real-time).
     */
    public LiveData<Result<Order>> getOrderDetail(String orderId) {
        MutableLiveData<Result<Order>> out = new MutableLiveData<>();
        out.setValue(Result.loading());

        dbRef.child("orders").child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            out.postValue(Result.error("Không tìm thấy đơn hàng"));
                            return;
                        }
                        Order order = mapSnapshotToOrder(snapshot);
                        if (order != null) {
                            out.postValue(Result.success(order));
                        } else {
                            out.postValue(Result.error("Lỗi đọc dữ liệu đơn hàng"));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        out.postValue(Result.error(error.getMessage()));
                    }
                });
        return out;
    }

    /**
     * Cập nhật trạng thái đơn hàng.
     * Ghi newStatus vào /orders/{orderId}/status.
     * Ghi log vào /orders/{orderId}/statusLogs.
     */
    public LiveData<Result<Void>> updateOrderStatus(String orderId, String newStatus) {
        return updateOrderStatus(orderId, newStatus, null);
    }

    public LiveData<Result<Void>> updateOrderStatus(String orderId, String newStatus, String failReason) {
        MutableLiveData<Result<Void>> out = new MutableLiveData<>();
        out.setValue(Result.loading());

        FirebaseUser currentUser = auth.getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "unknown";

        Map<String, Object> updates = new HashMap<>();
        updates.put("orders/" + orderId + "/status", newStatus);
        updates.put("orders/" + orderId + "/updatedAt", getCurrentIsoTime());
        if (failReason != null) {
            updates.put("orders/" + orderId + "/failReason", failReason);
        }

        // Ghi log
        String logKey = dbRef.child("orders").child(orderId).child("statusLogs").push().getKey();
        if (logKey != null) {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("status", newStatus);
            logEntry.put("changedBy", uid);
            logEntry.put("changedAt", getCurrentIsoTime());
            logEntry.put("role", "SHIPPER");
            if (failReason != null) {
                logEntry.put("failReason", failReason);
            }
            updates.put("orders/" + orderId + "/statusLogs/" + logKey, logEntry);
        }

        dbRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> out.postValue(Result.success(null)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "updateOrderStatus failed", e);
                    out.postValue(Result.error("Cập nhật thất bại: " + e.getMessage()));
                });

        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SHIPPER STATUS (Online/Offline)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cập nhật trạng thái online của shipper tại /users/{uid}/onlineStatus.
     */
    public void updateOnlineStatus(boolean isOnline) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String status = isOnline ? "AVAILABLE" : "OFFLINE";
        dbRef.child("users").child(currentUser.getUid())
                .child("onlineStatus")
                .setValue(status)
                .addOnFailureListener(e -> Log.e(TAG, "updateOnlineStatus failed", e));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Map DataSnapshot → Order DTO.
     * Hỗ trợ cả snake_case (firebase) và camelCase.
     */
    private Order mapSnapshotToOrder(DataSnapshot s) {
        try {
            Order o = new Order();
            o.setId(s.getKey());

            // Status & timestamps
            o.setStatus(stringVal(s, "status"));
            o.setCreatedAt(stringVal(s, "created_at", "createdAt"));
            o.setUpdatedAt(stringVal(s, "updated_at", "updatedAt"));

            // Shipper ID
            o.setShipperId(stringVal(s, "shipperId", "shipper_id"));

            // Recipient info
            o.setRecipientName(stringVal(s, "recipient_name", "recipientName"));
            o.setRecipientPhone(stringVal(s, "recipient_phone", "recipientPhone"));
            o.setRecipientAddress(stringVal(s, "recipient_address", "recipientAddress", "deliveryAddress"));
            o.setNote(stringVal(s, "note"));
            o.setFailReason(stringVal(s, "failReason", "fail_reason"));
            o.setPaymentMethod(stringVal(s, "paymentMethod", "payment_method"));
            o.setPaymentStatus(stringVal(s, "paymentStatus", "payment_status"));

            // Amount
            Double finalAmt = doubleVal(s, "final_amount", "finalAmount", "totalAmount", "total_amount");
            if (finalAmt != null) o.setFinalAmount(finalAmt);

            // Items
            DataSnapshot itemsSnap = s.child("items");
            if (itemsSnap.exists()) {
                List<vn.vuavuive.shared.data.dto.OrderItem> items = new ArrayList<>();
                for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                    vn.vuavuive.shared.data.dto.OrderItem item = new vn.vuavuive.shared.data.dto.OrderItem();
                    item.setProductId(stringVal(itemSnap, "productId", "product_id"));
                    item.setProductName(stringVal(itemSnap, "productName", "product_name", "name"));
                    item.setImageUrl(stringVal(itemSnap, "imageUrl", "image_url", "image"));

                    Integer qty = intVal(itemSnap, "quantity");
                    if (qty != null) item.setQuantity(qty);

                    Double price = doubleVal(itemSnap, "price", "productPrice", "unit_price");
                    if (price != null) item.setProductPrice(price);

                    items.add(item);
                }
                o.setItems(items);
            }

            return o;
        } catch (Exception e) {
            Log.e(TAG, "mapSnapshotToOrder error for key=" + s.getKey(), e);
            return null;
        }
    }

    /** Đọc String từ nhiều field name có thể (first match). */
    private String stringVal(DataSnapshot s, String... keys) {
        for (String key : keys) {
            Object val = s.child(key).getValue();
            if (val != null) return val.toString();
        }
        return null;
    }

    /** Đọc Double từ nhiều field name có thể. */
    private Double doubleVal(DataSnapshot s, String... keys) {
        for (String key : keys) {
            Object val = s.child(key).getValue();
            if (val instanceof Number) return ((Number) val).doubleValue();
        }
        return null;
    }

    /** Đọc Integer từ nhiều field name có thể. */
    private Integer intVal(DataSnapshot s, String... keys) {
        for (String key : keys) {
            Object val = s.child(key).getValue();
            if (val instanceof Number) return ((Number) val).intValue();
        }
        return null;
    }

    private String getCurrentIsoTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                java.util.Locale.getDefault()).format(new java.util.Date());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Result wrapper (giống pattern của app-customer)
    // ─────────────────────────────────────────────────────────────────────────

    public static class Result<T> {
        public enum Status { LOADING, SUCCESS, ERROR }
        public final Status status;
        public final T data;
        public final String message;

        private Result(Status status, T data, String message) {
            this.status = status;
            this.data = data;
            this.message = message;
        }

        public static <T> Result<T> loading() { return new Result<>(Status.LOADING, null, null); }
        public static <T> Result<T> success(T data) { return new Result<>(Status.SUCCESS, data, null); }
        public static <T> Result<T> error(String message) { return new Result<>(Status.ERROR, null, message); }
    }
}
