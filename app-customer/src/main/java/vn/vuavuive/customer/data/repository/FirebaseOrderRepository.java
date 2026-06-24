package vn.vuavuive.customer.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import vn.vuavuive.shared.data.dto.CreateMomoPaymentResponse;
import vn.vuavuive.shared.data.dto.DeliveryInfo;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.OrderItem;
import vn.vuavuive.shared.data.dto.PaymentDetail;
import vn.vuavuive.shared.data.dto.PaymentStatusResponse;
import vn.vuavuive.shared.data.dto.ReturnRequest;
import vn.vuavuive.shared.data.dto.Voucher;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest.OrderItemRequest;
import vn.vuavuive.shared.util.SessionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseOrderRepository {

    private static final String TAG = "FirebaseOrderRepository";
    private final DatabaseReference dbRef;
    private final SessionManager sessionManager;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private interface StockCallback {
        void onSuccess();
        void onFailure(String message);
    }

    private interface RollbackCallback {
        void onComplete();
    }

    @Inject
    public FirebaseOrderRepository(SessionManager sessionManager) {
        this.dbRef = FirebaseDatabase.getInstance().getReference();
        this.sessionManager = sessionManager;
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

    // ── Mapping Helpers ──────────────────────────────────────────────────────
    private Order mapSnapshotToOrder(DataSnapshot s) {
        Order o = new Order();
        o.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
        o.setOrderId(s.child("order_id").getValue(String.class));
        o.setUserId(s.child("user_id").getValue(String.class));
        o.setStatus(s.child("status").getValue(String.class));
        o.setNote(s.child("note").getValue(String.class));
        
        Double subtotal = s.child("subtotal_amount").getValue(Double.class);
        o.setSubtotal(subtotal != null ? subtotal : 0.0);
        
        Double shipping = s.child("shipping_fee").getValue(Double.class);
        o.setShippingFee(shipping != null ? shipping : 0.0);
        
        Double discount = s.child("discount_amount").getValue(Double.class);
        o.setDiscount(discount != null ? discount : 0.0);
        
        Double finalAmount = s.child("final_amount").getValue(Double.class);
        o.setFinalAmount(finalAmount != null ? finalAmount : 0.0);
        o.setTotalAmount(finalAmount != null ? finalAmount : 0.0);

        Boolean restored = s.child("stock_restored").getValue(Boolean.class);
        o.setStockRestored(restored != null ? restored : false);
        
        o.setCreatedAt(s.child("created_at").getValue(String.class));
        o.setUpdatedAt(s.child("updated_at").getValue(String.class));
        o.setDeliveredAt(s.child("delivered_at").getValue(String.class));

        // Delivery
        DeliveryInfo delivery = new DeliveryInfo();
        delivery.setName(s.child("delivery_name").getValue(String.class));
        delivery.setPhone(s.child("delivery_phone").getValue(String.class));
        delivery.setAddress(s.child("delivery_address").getValue(String.class));
        delivery.setNote(o.getNote());
        o.setDelivery(delivery);
        o.setDeliveryAddress(delivery.getAddress());
        o.setDeliveryName(delivery.getName());
        o.setDeliveryPhone(delivery.getPhone());

        // Payment Detail
        PaymentDetail payment = new PaymentDetail();
        payment.setMethod(s.child("payment_method").getValue(String.class));
        payment.setStatus(s.child("payment_status").getValue(String.class));
        payment.setAmount(o.getFinalAmount());
        o.setPayment(payment);
        o.setPaymentMethod(payment.getMethod());
        o.setPaymentStatus(payment.getStatus());

        // Items
        List<OrderItem> items = new ArrayList<>();
        DataSnapshot itemsSnap = s.child("items");
        if (itemsSnap.exists()) {
            for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                OrderItem item = new OrderItem();
                item.setProductId(itemSnap.child("product_id").getValue(String.class));
                item.setName(itemSnap.child("product_name").getValue(String.class));
                item.setImageUrl(itemSnap.child("image_url").getValue(String.class));
                item.setUnit(itemSnap.child("unit").getValue(String.class));
                
                Double price = itemSnap.child("unit_price").getValue(Double.class);
                item.setPrice(price != null ? price : 0.0);
                
                Integer qty = itemSnap.child("quantity").getValue(Integer.class);
                item.setQuantity(qty != null ? qty : 1);
                
                Double itemSubtotal = itemSnap.child("subtotal").getValue(Double.class);
                item.setSubtotal(itemSubtotal != null ? itemSubtotal : item.getPrice() * item.getQuantity());
                
                items.add(item);
            }
        }
        o.setItems(items);

        // Return request
        DataSnapshot returnSnap = s.child("return_request");
        if (returnSnap.exists()) {
            ReturnRequest returnRequest = new ReturnRequest();
            returnRequest.setReason(returnSnap.child("reason").getValue(String.class));
            returnRequest.setStatus(returnSnap.child("status").getValue(String.class));
            returnRequest.setAdminNote(returnSnap.child("admin_note").getValue(String.class));
            returnRequest.setRequestedAt(returnSnap.child("requested_at").getValue(String.class));
            o.setReturnRequest(returnRequest);
        }

        return o;
    }

    // ── Create Order with Stock Validation & Rollback ─────────────────────────
    public LiveData<AuthRepository.Result<Order>> createOrder(CreateOrderRequest request) {
        MutableLiveData<AuthRepository.Result<Order>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        String uid = getCurrentUserUid();
        if (uid == null) {
            result.postValue(AuthRepository.Result.error("Chưa đăng nhập"));
            return result;
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            result.postValue(AuthRepository.Result.error("Giỏ hàng của bạn đang trống"));
            return result;
        }

        // 1. Transactional check and decrement stock
        List<String> successfullyDecrementedIds = new ArrayList<>();
        List<Integer> successfullyDecrementedQtys = new ArrayList<>();

        decrementStockSequentially(request.getItems(), 0, successfullyDecrementedIds, successfullyDecrementedQtys, new StockCallback() {
            @Override
            public void onSuccess() {
                // 2. Once stock is confirmed, write Order node
                executor.execute(() -> {
                    try {
                        String orderUuid = UUID.randomUUID().toString();
                        String orderId = "ORD-" + System.currentTimeMillis();
                        String now = getCurrentIsoString();

                        Map<String, Object> orderMap = new HashMap<>();
                        orderMap.put("id", orderUuid);
                        orderMap.put("order_id", orderId);
                        orderMap.put("user_id", uid);
                        orderMap.put("user_name", sessionManager.getUser() != null ? sessionManager.getUser().getName() : "Khách Hàng");
                        orderMap.put("user_phone", sessionManager.getUser() != null ? sessionManager.getUser().getPhone() : "");

                        DeliveryInfo del = request.getDelivery();
                        orderMap.put("delivery_name", del != null ? del.getName() : "");
                        orderMap.put("delivery_phone", del != null ? del.getPhone() : "");
                        orderMap.put("delivery_address", del != null ? del.getAddress() : "");
                        orderMap.put("note", request.getNote() != null ? request.getNote() : "");

                        orderMap.put("subtotal_amount", request.getSubtotal());
                        orderMap.put("shipping_fee", request.getShippingFee());
                        orderMap.put("discount_amount", request.getDiscount());
                        orderMap.put("final_amount", request.getTotalAmount());

                        String rawMethod = request.getPayment() != null ? request.getPayment().get("method") : "cod";
                        String paymentMethod = rawMethod != null ? rawMethod.toUpperCase() : "COD";
                        
                        orderMap.put("payment_method", paymentMethod);
                        orderMap.put("payment_status", "UNPAID"); // default unpaid on creation
                        orderMap.put("status", "PENDING");
                        orderMap.put("stock_restored", false);
                        orderMap.put("created_at", now);
                        orderMap.put("updated_at", now);

                        // Items map
                        Map<String, Object> itemsMap = new HashMap<>();
                        for (OrderItemRequest item : request.getItems()) {
                            Map<String, Object> itemVal = new HashMap<>();
                            itemVal.put("product_id", item.getProductId());
                            itemVal.put("product_name", item.getProductName());
                            itemVal.put("unit_price", item.getPrice());
                            itemVal.put("quantity", item.getQuantity());
                            itemVal.put("subtotal", item.getSubtotal());
                            // Fetch product unit/image from sessionManager or mock values (fallback)
                            itemVal.put("unit", "gói");
                            itemVal.put("image_url", "");
                            itemsMap.put(item.getProductId(), itemVal);
                        }
                        orderMap.put("items", itemsMap);

                        // Status logs
                        String logUuid = UUID.randomUUID().toString();
                        Map<String, Object> logMap = new HashMap<>();
                        logMap.put("id", logUuid);
                        logMap.put("status", "PENDING");
                        logMap.put("note", "Đơn hàng vừa được tạo");
                        logMap.put("updated_by", uid);
                        logMap.put("updated_by_name", sessionManager.getUser() != null ? sessionManager.getUser().getName() : "Khách Hàng");
                        logMap.put("updated_by_role", "CUSTOMER");
                        logMap.put("created_at", now);

                        Map<String, Object> logsContainer = new HashMap<>();
                        logsContainer.put(logUuid, logMap);
                        orderMap.put("status_logs", logsContainer);

                        dbRef.child("orders").child(orderUuid).setValue(orderMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                dbRef.child("orders").child(orderUuid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Order finalOrder = mapSnapshotToOrder(snapshot);
                                        result.postValue(AuthRepository.Result.success(finalOrder));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        result.postValue(AuthRepository.Result.error("Lỗi đồng bộ đơn hàng: " + error.getMessage()));
                                    }
                                });
                            } else {
                                result.postValue(AuthRepository.Result.error("Lỗi ghi dữ liệu đơn hàng"));
                            }
                        });
                    } catch (Exception e) {
                        result.postValue(AuthRepository.Result.error("Lỗi khởi tạo đơn hàng: " + e.getMessage()));
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                result.postValue(AuthRepository.Result.error(message));
            }
        });

        return result;
    }

    private void decrementStockSequentially(List<OrderItemRequest> items, int index, List<String> successfullyDecrementedIds, List<Integer> successfullyDecrementedQtys, StockCallback callback) {
        if (index >= items.size()) {
            callback.onSuccess();
            return;
        }
        OrderItemRequest item = items.get(index);
        DatabaseReference productRef = dbRef.child("products").child(item.getProductId());
        productRef.child("stock_quantity").runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                Integer currentStock = currentData.getValue(Integer.class);
                if (currentStock == null) {
                    return com.google.firebase.database.Transaction.abort();
                }
                if (currentStock < item.getQuantity()) {
                    return com.google.firebase.database.Transaction.abort();
                }
                currentData.setValue(currentStock - item.getQuantity());
                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed && error == null) {
                    successfullyDecrementedIds.add(item.getProductId());
                    successfullyDecrementedQtys.add(item.getQuantity());
                    decrementStockSequentially(items, index + 1, successfullyDecrementedIds, successfullyDecrementedQtys, callback);
                } else {
                    rollbackStock(successfullyDecrementedIds, successfullyDecrementedQtys, 0, new RollbackCallback() {
                        @Override
                        public void onComplete() {
                            String errMsg = "Sản phẩm \"" + item.getProductName() + "\" không đủ số lượng tồn kho.";
                            callback.onFailure(errMsg);
                        }
                    });
                }
            }
        });
    }

    private void rollbackStock(List<String> productIds, List<Integer> quantities, int index, RollbackCallback callback) {
        if (index >= productIds.size()) {
            callback.onComplete();
            return;
        }
        String productId = productIds.get(index);
        int qty = quantities.get(index);
        dbRef.child("products").child(productId).child("stock_quantity").runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                Integer currentStock = currentData.getValue(Integer.class);
                if (currentStock == null) {
                    currentStock = 0;
                }
                currentData.setValue(currentStock + qty);
                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                rollbackStock(productIds, quantities, index + 1, callback);
            }
        });
    }

    // ── Get Orders for Current User ──────────────────────────────────────────
    public LiveData<AuthRepository.Result<List<Order>>> getOrders(String status, int page, int limit) {
        MutableLiveData<AuthRepository.Result<List<Order>>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        String uid = getCurrentUserUid();
        if (uid == null) {
            result.postValue(AuthRepository.Result.error("Chưa đăng nhập"));
            return result;
        }

        dbRef.child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                executor.execute(() -> {
                    List<Order> orders = new ArrayList<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        String orderUserId = s.child("user_id").getValue(String.class);
                        if (uid.equals(orderUserId)) {
                            Order order = mapSnapshotToOrder(s);
                            if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all") && !status.equalsIgnoreCase("Tất cả")) {
                                if (status.equalsIgnoreCase(order.getStatus())) {
                                    orders.add(order);
                                }
                            } else {
                                orders.add(order);
                            }
                        }
                    }

                    // Sort by created_at descending (newest first)
                    Collections.sort(orders, (o1, o2) -> {
                        String c1 = o1.getCreatedAt() != null ? o1.getCreatedAt() : "";
                        String c2 = o2.getCreatedAt() != null ? o2.getCreatedAt() : "";
                        return c2.compareTo(c1);
                    });

                    // Paginate
                    int startIndex = (page - 1) * limit;
                    if (startIndex >= orders.size()) {
                        result.postValue(AuthRepository.Result.success(new ArrayList<>()));
                    } else {
                        int endIndex = Math.min(startIndex + limit, orders.size());
                        result.postValue(AuthRepository.Result.success(orders.subList(startIndex, endIndex)));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.error("Lỗi kết nối Firebase: " + error.getMessage()));
            }
        });

        return result;
    }

    // ── Get Order Detail ─────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Order>> getOrderDetail(String orderId) {
        MutableLiveData<AuthRepository.Result<Order>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        dbRef.child("orders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Order order = mapSnapshotToOrder(snapshot);
                    result.postValue(AuthRepository.Result.success(order));
                } else {
                    result.postValue(AuthRepository.Result.error("Không tìm thấy đơn hàng"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.error("Lỗi đọc Firebase: " + error.getMessage()));
            }
        });

        return result;
    }

    // ── Cancel Order with Double-Restock Protection ──────────────────────────
    public LiveData<AuthRepository.Result<Void>> cancelOrder(String orderId) {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        dbRef.child("orders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    result.postValue(AuthRepository.Result.error("Đơn hàng không tồn tại"));
                    return;
                }

                String currentStatus = snapshot.child("status").getValue(String.class);
                Boolean restored = snapshot.child("stock_restored").getValue(Boolean.class);
                boolean isRestored = restored != null ? restored : false;

                if (!"PENDING".equalsIgnoreCase(currentStatus) && !"CONFIRMED".equalsIgnoreCase(currentStatus)) {
                    result.postValue(AuthRepository.Result.error("Không thể hủy đơn hàng ở trạng thái hiện tại (" + currentStatus + ")"));
                    return;
                }

                if (isRestored) {
                    dbRef.child("orders").child(orderId).child("status").setValue("CANCELLED");
                    result.postValue(AuthRepository.Result.success(null));
                    return;
                }

                // 1. Gather order items to restock
                List<String> productIds = new ArrayList<>();
                List<Integer> quantities = new ArrayList<>();

                DataSnapshot itemsSnap = snapshot.child("items");
                if (itemsSnap.exists()) {
                    for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                        String pid = itemSnap.child("product_id").getValue(String.class);
                        Integer qty = itemSnap.child("quantity").getValue(Integer.class);
                        if (pid != null && qty != null) {
                            productIds.add(pid);
                            quantities.add(qty);
                        }
                    }
                }

                // 2. Perform sequential stock restoration
                rollbackStock(productIds, quantities, 0, new RollbackCallback() {
                    @Override
                    public void onComplete() {
                        // 3. Mark status = CANCELLED and stock_restored = true
                        String now = getCurrentIsoString();
                        String uid = getCurrentUserUid();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", "CANCELLED");
                        updates.put("stock_restored", true);
                        updates.put("updated_at", now);

                        String logUuid = UUID.randomUUID().toString();
                        Map<String, Object> logMap = new HashMap<>();
                        logMap.put("id", logUuid);
                        logMap.put("status", "CANCELLED");
                        logMap.put("note", "Hủy đơn hàng");
                        logMap.put("updated_by", uid != null ? uid : "");
                        logMap.put("updated_by_name", sessionManager.getUser() != null ? sessionManager.getUser().getName() : "Khách Hàng");
                        logMap.put("updated_by_role", "CUSTOMER");
                        logMap.put("created_at", now);
                        updates.put("status_logs/" + logUuid, logMap);

                        dbRef.child("orders").child(orderId).updateChildren(updates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                result.postValue(AuthRepository.Result.success(null));
                            } else {
                                result.postValue(AuthRepository.Result.error("Lỗi cập nhật trạng thái hủy đơn"));
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.error("Lỗi truy xuất Firebase: " + error.getMessage()));
            }
        });

        return result;
    }

    // ── Return Request (No Stock Restock on Request) ──────────────────────────
    public LiveData<AuthRepository.Result<Void>> returnOrder(String orderId, String reason) {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        dbRef.child("orders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    result.postValue(AuthRepository.Result.error("Đơn hàng không tồn tại"));
                    return;
                }

                String currentStatus = snapshot.child("status").getValue(String.class);
                if (!"DELIVERED".equalsIgnoreCase(currentStatus)) {
                    result.postValue(AuthRepository.Result.error("Chỉ có thể yêu cầu trả hàng cho đơn đã nhận thành công"));
                    return;
                }

                String now = getCurrentIsoString();
                String uid = getCurrentUserUid();
                Map<String, Object> returnMap = new HashMap<>();
                returnMap.put("reason", reason);
                returnMap.put("status", "PENDING");
                returnMap.put("requested_at", now);

                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "RETURN_REQUESTED");
                updates.put("return_request", returnMap);
                updates.put("updated_at", now);

                String logUuid = UUID.randomUUID().toString();
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("id", logUuid);
                logMap.put("status", "RETURN_REQUESTED");
                logMap.put("note", "Yêu cầu trả hàng: " + reason);
                logMap.put("updated_by", uid != null ? uid : "");
                logMap.put("updated_by_name", sessionManager.getUser() != null ? sessionManager.getUser().getName() : "Khách Hàng");
                logMap.put("updated_by_role", "CUSTOMER");
                logMap.put("created_at", now);
                updates.put("status_logs/" + logUuid, logMap);

                dbRef.child("orders").child(orderId).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        result.postValue(AuthRepository.Result.success(null));
                    } else {
                        result.postValue(AuthRepository.Result.error("Lỗi gửi yêu cầu trả hàng"));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.error(error.getMessage()));
            }
        });

        return result;
    }

    // ── Mock VNPay and MoMo Payment Flow ─────────────────────────────────────
    public LiveData<AuthRepository.Result<String>> getVnpayUrl(String orderId) {
        MutableLiveData<AuthRepository.Result<String>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        String now = getCurrentIsoString();
        Map<String, Object> updates = new HashMap<>();
        updates.put("payment_status", "PAID");
        updates.put("status", "CONFIRMED");
        updates.put("payment_method", "VNPAY_MOCK");
        updates.put("mock_payment", true);
        updates.put("updated_at", now);

        dbRef.child("orders").child(orderId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String mockUrl = "https://example.com/api/payments/vnpay/return?vnp_ResponseCode=00&orderId=" + orderId;
                result.postValue(AuthRepository.Result.success(mockUrl));
            } else {
                result.postValue(AuthRepository.Result.error("Lỗi cập nhật thanh toán mock"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Result<String>> getMomoUrl(String orderId) {
        MutableLiveData<AuthRepository.Result<String>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        String mockUrl = "https://example.com/api/payments/momo/return?status=PAID&orderId=" + orderId;
        result.postValue(AuthRepository.Result.success(mockUrl));
        return result;
    }

    public LiveData<AuthRepository.Result<CreateMomoPaymentResponse>> createMomoPayment(
            String orderId, double amount, String userId) {
        MutableLiveData<AuthRepository.Result<CreateMomoPaymentResponse>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        String now = getCurrentIsoString();
        Map<String, Object> updates = new HashMap<>();
        updates.put("payment_status", "PAID");
        updates.put("status", "CONFIRMED");
        updates.put("payment_method", "MOMO_MOCK");
        updates.put("mock_payment", true);
        updates.put("updated_at", now);

        dbRef.child("orders").child(orderId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                CreateMomoPaymentResponse resp = new CreateMomoPaymentResponse();
                resp.setPayUrl("https://example.com/api/payments/momo/return?status=PAID&orderId=" + orderId);
                resp.setDeeplink("momo://payment?orderId=" + orderId);
                result.postValue(AuthRepository.Result.success(resp));
            } else {
                result.postValue(AuthRepository.Result.error("Lỗi cập nhật thanh toán mock"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Result<PaymentStatusResponse>> getPaymentStatus(String orderId) {
        MutableLiveData<AuthRepository.Result<PaymentStatusResponse>> result = new MutableLiveData<>();
        
        dbRef.child("orders").child(orderId).child("payment_status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                PaymentStatusResponse resp = new PaymentStatusResponse();
                resp.setPaymentStatus(status != null ? status : "UNPAID");
                result.postValue(AuthRepository.Result.success(resp));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.postValue(AuthRepository.Result.error(error.getMessage()));
            }
        });

        return result;
    }

    // ── Available Vouchers (Mock data for offline checkout) ─────────────────
    public LiveData<AuthRepository.Result<List<Voucher>>> getAvailableVouchers() {
        MutableLiveData<AuthRepository.Result<List<Voucher>>> result = new MutableLiveData<>();
        List<Voucher> list = new ArrayList<>();
        
        Voucher v1 = new Voucher();
        v1.setId("v1");
        v1.setCode("VUAVUIVE");
        v1.setNote("Giảm giá 15% tổng hóa đơn");
        v1.setType("PERCENTAGE");
        v1.setValue(15.0);
        list.add(v1);

        Voucher v2 = new Voucher();
        v2.setId("v2");
        v2.setCode("FREESHIP24");
        v2.setNote("Miễn phí vận chuyển");
        v2.setType("FIXED");
        v2.setValue(30000.0);
        list.add(v2);

        result.postValue(AuthRepository.Result.success(list));
        return result;
    }

    // ── Order Reviews ────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> submitReview(
            String orderId, List<Map<String, Object>> reviews) {
        MutableLiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> result = new MutableLiveData<>();
        dbRef.child("orders").child(orderId).child("reviewed").setValue(true);
        result.postValue(AuthRepository.Result.success(null));
        return result;
    }

    public LiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> getMyReview(String orderId) {
        MutableLiveData<AuthRepository.Result<vn.vuavuive.shared.data.dto.Review>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.success(null));
        return result;
    }
}
