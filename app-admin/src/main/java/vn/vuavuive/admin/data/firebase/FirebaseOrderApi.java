package vn.vuavuive.admin.data.firebase;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.Review;
import vn.vuavuive.shared.data.dto.Voucher;
import vn.vuavuive.shared.data.dto.request.CreateOrderRequest;

public class FirebaseOrderApi implements OrderApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    private String firstString(DataSnapshot snapshot, String... keys) {
        for (String key : keys) {
            String value = snapshot.child(key).getValue(String.class);
            if (value != null && !value.isEmpty()) return value;
        }
        return null;
    }

    private Order mapSnapshotToOrder(DataSnapshot s) {
        Order o = new Order();
        o.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
        o.setOrderId(s.child("order_id").getValue(String.class));
        o.setUserId(s.child("user_id").getValue(String.class));
        
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
        delivery.setName(firstString(s, "delivery_name", "deliveryName", "delivery/name"));
        delivery.setPhone(firstString(s, "delivery_phone", "deliveryPhone", "delivery/phone"));
        delivery.setAddress(firstString(s, "delivery_address", "deliveryAddress", "delivery/address"));
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
                item.setProductId(firstString(itemSnap, "product_id", "productId"));
                item.setName(firstString(itemSnap, "product_name", "productName", "name"));
                item.setImageUrl(firstString(itemSnap, "image_url", "imageUrl", "product_image_url", "productImageUrl"));
                item.setUnit(itemSnap.child("unit").getValue(String.class));
                
                Double price = itemSnap.child("unit_price").getValue(Double.class);
                if (price == null) price = itemSnap.child("unitPrice").getValue(Double.class);
                if (price == null) price = itemSnap.child("price").getValue(Double.class);
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

    @Override
    public Call<ApiResponse<Order>> getOrderDetail(String id) {
        return new Call<ApiResponse<Order>>() {
            @Override public Response<ApiResponse<Order>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Order>> callback) {
                dbRef.child("orders").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Order o = mapSnapshotToOrder(snapshot);
                            callback.onResponse(null, Response.success(ApiResponse.success(o, "success", null, null)));
                        } else {
                            callback.onResponse(null, Response.success(ApiResponse.error("Order not found")));
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

    // ── Unused/Empty implementations for Admin ───────────────────────────────

    @Override
    public Call<ApiResponse<Order>> createOrder(CreateOrderRequest body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<List<Order>>> getMyOrders(String status, int page, int limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Order>> cancelOrder(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Order>> returnRequest(String id, Map<String, String> body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Review>> submitReview(String id, Map<String, Object> body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Review>> getMyReview(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<List<Voucher>>> getAvailableVouchers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Voucher>> validateVoucher(Map<String, String> body) {
        throw new UnsupportedOperationException();
    }
}
