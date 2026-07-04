package vn.vuavuive.admin.data.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.AdminOrderApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;

public class FirebaseAdminOrderApi implements AdminOrderApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private interface RollbackCallback {
        void onComplete();
    }

    private String getCurrentUserUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private String getCurrentIsoString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private boolean matchesStatus(@Nullable String filter, @Nullable String orderStatus) {
        if (filter == null || filter.isEmpty() || "all".equalsIgnoreCase(filter)) return true;
        if ("pending".equalsIgnoreCase(filter)) {
            return "pending".equalsIgnoreCase(orderStatus)
                    || "pending_payment".equalsIgnoreCase(orderStatus)
                    || "pending_approval".equalsIgnoreCase(orderStatus);
        }
        return filter.equalsIgnoreCase(orderStatus);
    }

    private Order mapSnapshotToOrder(DataSnapshot s) {
        Order o = new Order();
        o.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
        o.setOrderId(s.child("order_id").getValue(String.class));
        o.setUserId(s.child("user_id").getValue(String.class));
        String shipperId = s.child("shipper_id").getValue(String.class);
        if (shipperId == null) shipperId = s.child("shipperId").getValue(String.class);
        o.setShipperId(shipperId);
        
        String status = s.child("status").getValue(String.class);
        o.setStatus(status != null ? status.toUpperCase() : null);
        
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
        vn.vuavuive.shared.data.dto.DeliveryInfo delivery = new vn.vuavuive.shared.data.dto.DeliveryInfo();
        delivery.setName(s.child("delivery_name").getValue(String.class));
        delivery.setPhone(s.child("delivery_phone").getValue(String.class));
        delivery.setAddress(s.child("delivery_address").getValue(String.class));
        delivery.setNote(o.getNote());
        o.setDelivery(delivery);
        o.setDeliveryAddress(delivery.getAddress());
        o.setDeliveryName(delivery.getName());
        o.setDeliveryPhone(delivery.getPhone());

        // Payment Detail
        vn.vuavuive.shared.data.dto.PaymentDetail payment = new vn.vuavuive.shared.data.dto.PaymentDetail();
        payment.setMethod(s.child("payment_method").getValue(String.class));
        payment.setStatus(s.child("payment_status").getValue(String.class));
        payment.setAmount(o.getFinalAmount());
        o.setPayment(payment);
        o.setPaymentMethod(payment.getMethod());
        o.setPaymentStatus(payment.getStatus());

        // Items
        List<vn.vuavuive.shared.data.dto.OrderItem> items = new ArrayList<>();
        DataSnapshot itemsSnap = s.child("items");
        if (itemsSnap.exists()) {
            for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                vn.vuavuive.shared.data.dto.OrderItem item = new vn.vuavuive.shared.data.dto.OrderItem();
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

        return o;
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

    @Override
    public Call<ApiResponse<List<Order>>> getOrders(
            String status, int page, int limit, String from, String to) {
        return new Call<ApiResponse<List<Order>>>() {
            @Override public Response<ApiResponse<List<Order>>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<List<Order>>> callback) {
                dbRef.child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Order> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Order o = mapSnapshotToOrder(s);
                            
                            // Filter by status
                            if (!matchesStatus(status, o.getStatus())) continue;
                            
                            // Filter by date range (lexicographically since ISO format is used)
                            if (from != null && !from.isEmpty() && o.getCreatedAt() != null) {
                                if (o.getCreatedAt().compareTo(from) < 0) {
                                    continue;
                                }
                            }
                            if (to != null && !to.isEmpty() && o.getCreatedAt() != null) {
                                if (o.getCreatedAt().compareTo(to) > 0) {
                                    continue;
                                }
                            }
                            
                            list.add(o);
                        }

                        // Sort newest first
                        list.sort((o1, o2) -> {
                            String c1 = o1.getCreatedAt() != null ? o1.getCreatedAt() : "";
                            String c2 = o2.getCreatedAt() != null ? o2.getCreatedAt() : "";
                            return c2.compareTo(c1);
                        });

                        // Paginate
                        int startIndex = 0;
                        int calculatedLimit = list.size();
                        if (page > 0 && limit > 0) {
                            startIndex = (page - 1) * limit;
                            calculatedLimit = limit;
                        }
                        
                        List<Order> paginated = new ArrayList<>();
                        if (startIndex < list.size()) {
                            int endIndex = Math.min(startIndex + calculatedLimit, list.size());
                            paginated = list.subList(startIndex, endIndex);
                        }

                        callback.onResponse(null, Response.success(ApiResponse.success(paginated, "success", null, null)));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<List<Order>>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Void>> bulkUpdateStatus(Map<String, Object> body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ResponseBody> exportOrders() {
        return new FirebaseCall<>(ResponseBody.create("id,status,total\n", okhttp3.MediaType.parse("text/csv")));
    }

    @Override
    public Call<Map<String, String>> assignShipper(String orderId, String shipperId) {
        return new Call<Map<String, String>>() {
            @Override public Response<Map<String, String>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<Map<String, String>> callback) {
                dbRef.child("users").child(shipperId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnap) {
                        if (!userSnap.exists() || !"SHIPPER".equalsIgnoreCase(userSnap.child("role").getValue(String.class))) {
                            callback.onResponse(null, Response.success(Collections.singletonMap("error", "Shipper khong hop le")));
                            return;
                        }
                        Boolean active = userSnap.child("is_active").getValue(Boolean.class);
                        if (active == null) active = userSnap.child("isActive").getValue(Boolean.class);
                        if (Boolean.FALSE.equals(active)) {
                            callback.onResponse(null, Response.success(Collections.singletonMap("error", "Shipper dang bi khoa")));
                            return;
                        }

                        String name = userSnap.child("name").getValue(String.class);
                        if (name == null || name.isEmpty()) name = userSnap.child("full_name").getValue(String.class);
                        if (name == null || name.isEmpty()) name = userSnap.child("email").getValue(String.class);
                        final String shipperName = name;

                        dbRef.child("orders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot orderSnap) {
                                if (!orderSnap.exists()) {
                                    callback.onResponse(null, Response.success(Collections.singletonMap("error", "Order not found")));
                                    return;
                                }
                                String status = orderSnap.child("status").getValue(String.class);
                                if (!isAssignableStatus(status)) {
                                    callback.onResponse(null, Response.success(Collections.singletonMap("error", "Trang thai don khong cho gan shipper")));
                                    return;
                                }

                                String now = getCurrentIsoString();
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("shipper_id", shipperId);
                                updates.put("shipperId", shipperId);
                                updates.put("shipper_name", shipperName);
                                updates.put("shipperName", shipperName);
                                updates.put("status", "SHIPPING");
                                updates.put("updated_at", now);

                                String logUuid = UUID.randomUUID().toString();
                                Map<String, Object> logMap = new HashMap<>();
                                logMap.put("id", logUuid);
                                logMap.put("status", "SHIPPING");
                                logMap.put("note", "Gan shipper: " + (shipperName != null ? shipperName : shipperId));
                                logMap.put("updated_by", getCurrentUserUid() != null ? getCurrentUserUid() : "");
                                logMap.put("updated_by_role", "ADMIN");
                                logMap.put("created_at", now);
                                updates.put("status_logs/" + logUuid, logMap);

                                dbRef.child("orders").child(orderId).updateChildren(updates).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        callback.onResponse(null, Response.success(Collections.singletonMap("message", "Gan shipper thanh cong")));
                                    } else {
                                        callback.onResponse(null, Response.success(Collections.singletonMap("error", "Khong gan duoc shipper")));
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onResponse(null, Response.success(Collections.singletonMap("error", error.getMessage())));
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResponse(null, Response.success(Collections.singletonMap("error", error.getMessage())));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<Map<String, String>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    private boolean isAssignableStatus(String status) {
        if (status == null) return false;
        String s = status.toUpperCase(Locale.US);
        return "CONFIRMED".equals(s) || "PREPARING".equals(s) || "READY_FOR_PICKUP".equals(s) || "SHIPPING".equals(s);
    }

    @Override
    public Call<ApiResponse<Order>> updateOrderStatus(String id, Map<String, String> body) {
        String inputStatus = body.get("status");
        final String newStatus = inputStatus != null ? inputStatus.toUpperCase() : "PENDING";
        
        return new Call<ApiResponse<Order>>() {
            @Override public Response<ApiResponse<Order>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Order>> callback) {
                dbRef.child("orders").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onResponse(null, Response.success(ApiResponse.error("Order not found")));
                            return;
                        }
                        
                        Boolean restored = snapshot.child("stock_restored").getValue(Boolean.class);
                        boolean isRestored = restored != null ? restored : false;

                        if ("CANCELLED".equals(newStatus)) {
                            if (isRestored) {
                                // Already restored, just update status
                                updateOrderStatusAndLog(id, newStatus, "Hủy đơn hàng", callback);
                            } else {
                                // Need to restore stock
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
                                rollbackStock(productIds, quantities, 0, () -> {
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("stock_restored", true);
                                    dbRef.child("orders").child(id).updateChildren(updates).addOnCompleteListener(t -> {
                                        updateOrderStatusAndLog(id, newStatus, "Hủy đơn hàng và hoàn kho", callback);
                                    });
                                });
                            }
                        } else {
                            updateOrderStatusAndLog(id, newStatus, "Cập nhật trạng thái thành " + newStatus, callback);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<Order>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    private void updateOrderStatusAndLog(String orderId, String status, String note, Callback<ApiResponse<Order>> callback) {
        String uid = getCurrentUserUid();
        String now = getCurrentIsoString();
        
        // Let's fetch the operator's profile
        dbRef.child("users").child(uid != null ? uid : "").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String operatorName = "Admin";
                String operatorRole = "ADMIN";
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null) operatorName = name;
                    String role = snapshot.child("role").getValue(String.class);
                    if (role != null) operatorRole = role.toUpperCase();
                }
                
                final String finalName = operatorName;
                final String finalRole = operatorRole;
                
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", status);
                updates.put("updated_at", now);
                if ("DELIVERED".equals(status)) {
                    updates.put("delivered_at", now);
                }

                String logUuid = UUID.randomUUID().toString();
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("id", logUuid);
                logMap.put("status", status);
                logMap.put("note", note);
                logMap.put("updated_by", uid != null ? uid : "");
                logMap.put("updated_by_name", finalName);
                logMap.put("updated_by_role", finalRole);
                logMap.put("created_at", now);
                
                updates.put("status_logs/" + logUuid, logMap);
                
                dbRef.child("orders").child(orderId).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Read updated order to return
                        dbRef.child("orders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot s) {
                                Order updatedOrder = mapSnapshotToOrder(s);
                                callback.onResponse(null, Response.success(ApiResponse.success(updatedOrder, "success", null, null)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                            }
                        });
                    } else {
                        callback.onResponse(null, Response.success(ApiResponse.error("Failed to update Firebase order")));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
            }
        });
    }

    @Override
    public Call<ApiResponse<Order>> reviewReturnRequest(String id, Map<String, String> body) {
        String action = body.get("action"); // "approve" or "reject"
        String note = body.get("note");
        final String newStatus = "approve".equalsIgnoreCase(action) ? "RETURN_APPROVED" : "DELIVERED";
        final String noteLog = "approve".equalsIgnoreCase(action) ? "Đồng ý trả hàng: " + note : "Từ chối trả hàng: " + note;
        
        return new Call<ApiResponse<Order>>() {
            @Override public Response<ApiResponse<Order>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Order>> callback) {
                // If return approved, restore stock!
                if ("RETURN_APPROVED".equals(newStatus)) {
                    dbRef.child("orders").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                callback.onResponse(null, Response.success(ApiResponse.error("Order not found")));
                                return;
                            }
                            
                            Boolean restored = snapshot.child("stock_restored").getValue(Boolean.class);
                            boolean isRestored = restored != null ? restored : false;

                            if (isRestored) {
                                updateOrderStatusAndLog(id, newStatus, noteLog, callback);
                            } else {
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
                                rollbackStock(productIds, quantities, 0, () -> {
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("stock_restored", true);
                                    updates.put("return_request/status", "APPROVED");
                                    dbRef.child("orders").child(id).updateChildren(updates).addOnCompleteListener(t -> {
                                        updateOrderStatusAndLog(id, newStatus, noteLog, callback);
                                    });
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                        }
                    });
                } else {
                    // RETURN_REJECTED / back to DELIVERED
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("return_request/status", "REJECTED");
                    dbRef.child("orders").child(id).updateChildren(updates).addOnCompleteListener(t -> {
                        updateOrderStatusAndLog(id, "RETURN_REJECTED", noteLog, callback);
                    });
                }
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<Order>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Order>> markPaid(String id) {
        return new Call<ApiResponse<Order>>() {
            @Override public Response<ApiResponse<Order>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Order>> callback) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("payment_status", "PAID");
                updates.put("payment/status", "PAID");
                dbRef.child("orders").child(id).updateChildren(updates).addOnCompleteListener(task -> {
                    updateOrderStatusAndLog(id, "CONFIRMED", "Đánh dấu đã thanh toán", callback);
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<Order>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Order>> markRefunded(String id) {
        return new Call<ApiResponse<Order>>() {
            @Override public Response<ApiResponse<Order>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Order>> callback) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("payment_status", "REFUNDED");
                updates.put("payment/status", "REFUNDED");
                dbRef.child("orders").child(id).updateChildren(updates).addOnCompleteListener(task -> {
                    updateOrderStatusAndLog(id, "CANCELLED", "Đánh dấu đã hoàn tiền", callback);
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<Order>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }
}
