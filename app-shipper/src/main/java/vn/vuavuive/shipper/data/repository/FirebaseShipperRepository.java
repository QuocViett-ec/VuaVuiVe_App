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

import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.api.ShipperOrderApi;
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
    private final ShipperOrderApi shipperOrderApi;

    @Inject
    public FirebaseShipperRepository(
            SessionManager sessionManager,
            ShipperOrderApi shipperOrderApi) {
        this.sessionManager = sessionManager;
        this.shipperOrderApi = shipperOrderApi;
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
    private void fetchUserProfileByEmailOrUid(String email, String uid, String idToken, MutableLiveData<Result<User>> out) {
        dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot usersSnap) {
                DataSnapshot matchedSnap = null;
                if (usersSnap.exists()) {
                    for (DataSnapshot child : usersSnap.getChildren()) {
                        String uEmail = child.child("email").getValue(String.class);
                        String uRole = child.child("role").getValue(String.class);
                        if (uEmail != null && uEmail.equalsIgnoreCase(email) && "SHIPPER".equalsIgnoreCase(uRole)) {
                            matchedSnap = child;
                            break;
                        }
                    }
                }

                String finalId = uid;
                String name = "Shipper Vui Vẻ";
                String phone = "";

                if (matchedSnap != null) {
                    finalId = matchedSnap.getKey();
                    name = matchedSnap.child("name").getValue(String.class);
                    if (name == null || name.isEmpty()) {
                        name = matchedSnap.child("full_name").getValue(String.class);
                    }
                    phone = matchedSnap.child("phone").getValue(String.class);
                } else {
                    java.util.Map<String, Object> shipperMap = new java.util.HashMap<>();
                    shipperMap.put("id", uid);
                    shipperMap.put("name", name);
                    shipperMap.put("email", email);
                    shipperMap.put("role", "SHIPPER");
                    shipperMap.put("isActive", true);
                    shipperMap.put("is_active", true);
                    dbRef.child("users").child(uid).setValue(shipperMap);
                }

                User user = new User();
                user.setId(finalId);
                user.setEmail(email);
                user.setName(name != null ? name : email);
                user.setPhone(phone);
                user.setRole("SHIPPER");

                if (sessionManager.saveSession(user, idToken, null)) {
                    out.setValue(Result.success(user));
                } else {
                    logout();
                    out.setValue(Result.error("Phiên đăng nhập không hợp lệ"));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logout();
                out.setValue(Result.error("Lỗi đọc dữ liệu: " + error.getMessage()));
            }
        });
    }

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
                    firebaseUser.getIdToken(false).addOnSuccessListener(tokenResult -> {
                        fetchUserProfileByEmailOrUid(email, firebaseUser.getUid(), tokenResult.getToken(), out);
                    }).addOnFailureListener(e -> {
                        logout();
                        out.setValue(Result.error("Không lấy được token đăng nhập"));
                    });
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Login failed, trying to auto-register shipper", e);
                    if (email.toLowerCase(java.util.Locale.ROOT).contains("shipper")) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(createResult -> {
                                FirebaseUser firebaseUser = createResult.getUser();
                                if (firebaseUser != null) {
                                    firebaseUser.getIdToken(false).addOnSuccessListener(tokenResult -> {
                                        fetchUserProfileByEmailOrUid(email, firebaseUser.getUid(), tokenResult.getToken(), out);
                                    }).addOnFailureListener(err -> {
                                        logout();
                                        out.setValue(Result.error("Không lấy được token đăng nhập"));
                                    });
                                } else {
                                    out.setValue(Result.error("Sai email hoặc mật khẩu"));
                                }
                            })
                            .addOnFailureListener(err -> {
                                out.setValue(Result.error("Sai email hoặc mật khẩu: " + err.getMessage()));
                            });
                    } else {
                        out.setValue(Result.error("Sai email hoặc mật khẩu: " + e.getMessage()));
                    }
                });

        return out;
    }

    /**
     * Kiểm tra Firebase session còn hợp lệ.
     * FirebaseAuth tự refresh token nên chỉ cần check currentUser != null và role trong session.
     */
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null && sessionManager.isLoggedIn() && sessionManager.isShipper();
    }

    /**
     * Đăng xuất Firebase Auth và xoá session local.
     */
    public void logout() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String uid = sessionManager.getUser() != null ? sessionManager.getUser().getId() : currentUser.getUid();
            DatabaseReference statusRef = dbRef.child("users").child(uid)
                    .child("onlineStatus");
            statusRef.onDisconnect().cancel();
            statusRef.setValue("OFFLINE")
                    .addOnFailureListener(e -> Log.e(TAG, "set offline before logout failed", e));
        }
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
                String uid = sessionManager.getUser() != null ? sessionManager.getUser().getId() : currentUser.getUid();

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

    /** Lắng nghe real-time chi tiết một đơn hàng. */
    public LiveData<Result<Order>> getOrderDetail(String orderId) {
        return new LiveData<Result<Order>>() {
            private final DatabaseReference orderRef = dbRef.child("orders").child(orderId);
            private ValueEventListener listener;

            @Override
            protected void onActive() {
                super.onActive();
                postValue(Result.loading());
                listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            postValue(Result.error("Không tìm thấy đơn hàng"));
                            return;
                        }
                        Order order = mapSnapshotToOrder(snapshot);
                        if (order != null) {
                            postValue(Result.success(order));
                        } else {
                            postValue(Result.error("Lỗi đọc dữ liệu đơn hàng"));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        postValue(Result.error(error.getMessage()));
                    }
                };
                orderRef.addValueEventListener(listener);
            }

            @Override
            protected void onInactive() {
                super.onInactive();
                if (listener != null) {
                    orderRef.removeEventListener(listener);
                    listener = null;
                }
            }
        };
    }

    /** Backend xác thực transition rồi ghi Firebase để mọi app nhận realtime. */
    public LiveData<Result<Void>> updateOrderStatus(String orderId, String newStatus) {
        return updateOrderStatus(orderId, newStatus, null);
    }

    public LiveData<Result<Void>> updateOrderStatus(String orderId, String newStatus, String failReason) {
        MutableLiveData<Result<Void>> out = new MutableLiveData<>();
        out.setValue(Result.loading());

        FirebaseUser currentUser = auth.getCurrentUser();
        String uid = sessionManager.getUser() != null ? sessionManager.getUser().getId() : (currentUser != null ? currentUser.getUid() : "unknown");

        shipperOrderApi.updateDeliveryStatus(uid, orderId, newStatus, failReason)
                .enqueue(new retrofit2.Callback<ApiResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<ApiResponse<Map<String, String>>> call,
                            retrofit2.Response<ApiResponse<Map<String, String>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            out.postValue(Result.success(null));
                        } else {
                            out.postValue(Result.error(
                                    "Backend từ chối cập nhật (" + response.code() + ")"));
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<ApiResponse<Map<String, String>>> call,
                            Throwable error) {
                        Log.e(TAG, "updateOrderStatus failed", error);
                        out.postValue(Result.error("Cập nhật thất bại: " + error.getMessage()));
                    }
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
        String uid = sessionManager.getUser() != null ? sessionManager.getUser().getId() : currentUser.getUid();
        DatabaseReference statusRef = dbRef.child("users").child(uid)
                .child("onlineStatus");
        if (isOnline) {
            statusRef.onDisconnect().setValue("OFFLINE");
        } else {
            statusRef.onDisconnect().cancel();
        }
        statusRef
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

            // Recipient info: customer orders store these as delivery_*.
            o.setRecipientName(stringVal(s, "recipient_name", "recipientName", "delivery_name", "deliveryName", "delivery/name"));
            o.setRecipientPhone(stringVal(s, "recipient_phone", "recipientPhone", "delivery_phone", "deliveryPhone", "delivery/phone"));
            o.setRecipientAddress(stringVal(s, "recipient_address", "recipientAddress", "delivery_address", "deliveryAddress", "delivery/address"));
            o.setNote(stringVal(s, "note"));
            o.setFailReason(stringVal(s, "failReason", "fail_reason"));
            o.setPaymentMethod(stringVal(s, "paymentMethod", "payment_method", "payment/method"));
            o.setPaymentStatus(stringVal(s, "paymentStatus", "payment_status", "payment/status"));

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
                    item.setImageUrl(stringVal(itemSnap, "imageUrl", "image_url", "productImageUrl", "product_image_url", "image"));

                    Integer qty = intVal(itemSnap, "quantity");
                    if (qty != null) item.setQuantity(qty);

                    Double price = doubleVal(itemSnap, "price", "productPrice", "unit_price", "unitPrice");
                    if (price != null) item.setProductPrice(price);

                    Double subtotal = doubleVal(itemSnap, "subtotal", "lineTotal");
                    if (subtotal != null) item.setSubtotal(subtotal);

                    items.add(item);
                }
                o.setItems(items);
            }

            DataSnapshot returnSnap = s.child("return_request");
            if (returnSnap.exists()) {
                vn.vuavuive.shared.data.dto.ReturnRequest request =
                        new vn.vuavuive.shared.data.dto.ReturnRequest();
                request.setReason(returnSnap.child("reason").getValue(String.class));
                request.setStatus(returnSnap.child("status").getValue(String.class));
                request.setAdminNote(returnSnap.child("admin_note").getValue(String.class));
                request.setRequestedAt(returnSnap.child("requested_at").getValue(String.class));
                o.setReturnRequest(request);
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
