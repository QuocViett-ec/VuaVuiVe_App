package vn.vuavuive.admin.data.firebase;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.AdminShipmentApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Shipment;

public class FirebaseAdminShipmentApi implements AdminShipmentApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    private Shipment mapSnapshotToShipment(DataSnapshot s) {
        Shipment sh = new Shipment();
        sh.setId(s.getKey());
        sh.setOrderId(s.child("orderId").getValue(String.class) != null ? s.child("orderId").getValue(String.class) : s.child("order_id").getValue(String.class));
        sh.setCustomerId(s.child("customerId").getValue(String.class) != null ? s.child("customerId").getValue(String.class) : s.child("customer_id").getValue(String.class));
        sh.setCarrier(s.child("carrier").getValue(String.class));
        sh.setTrackingNumber(s.child("trackingNumber").getValue(String.class) != null ? s.child("trackingNumber").getValue(String.class) : s.child("tracking_number").getValue(String.class));
        
        Double fee = s.child("shippingFee").getValue(Double.class);
        if (fee == null) fee = s.child("shipping_fee").getValue(Double.class);
        sh.setShippingFee(fee != null ? fee : 0.0);
        
        sh.setEta(s.child("eta").getValue(String.class));
        sh.setDeliveredAt(s.child("deliveredAt").getValue(String.class) != null ? s.child("deliveredAt").getValue(String.class) : s.child("delivered_at").getValue(String.class));
        sh.setCurrentStatus(s.child("currentStatus").getValue(String.class) != null ? s.child("currentStatus").getValue(String.class) : s.child("current_status").getValue(String.class));
        sh.setCreatedAt(s.child("createdAt").getValue(String.class) != null ? s.child("createdAt").getValue(String.class) : s.child("created_at").getValue(String.class));
        return sh;
    }

    @Override
    public Call<ApiResponse<List<Shipment>>> getAllShipments(int page, int limit, String status) {
        return new Call<ApiResponse<List<Shipment>>>() {
            @Override public Response<ApiResponse<List<Shipment>>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<List<Shipment>>> callback) {
                dbRef.child("shipments").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Shipment> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Shipment sh = mapSnapshotToShipment(s);
                            list.add(sh);
                        }

                        // Filter by status
                        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
                            List<Shipment> filtered = new ArrayList<>();
                            for (Shipment sh : list) {
                                if (status.equalsIgnoreCase(sh.getCurrentStatus())) {
                                    filtered.add(sh);
                                }
                            }
                            list = filtered;
                        }

                        // Paginate
                        int startIndex = (page - 1) * limit;
                        List<Shipment> paginated = new ArrayList<>();
                        if (startIndex < list.size()) {
                            int endIndex = Math.min(startIndex + limit, list.size());
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
            @NonNull @Override public Call<ApiResponse<List<Shipment>>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Shipment>> createShipment(Map<String, Object> body) {
        String id = "ship-" + UUID.randomUUID().toString();
        String orderId = (String) body.get("orderId");
        if (orderId == null) orderId = (String) body.get("order_id");

        Map<String, Object> sMap = new HashMap<>();
        sMap.put("id", id);
        sMap.put("orderId", orderId);
        sMap.put("customerId", body.get("customerId") != null ? body.get("customerId") : body.get("customer_id"));
        sMap.put("carrier", body.get("carrier"));
        sMap.put("trackingNumber", "TRK" + System.currentTimeMillis());
        sMap.put("shippingFee", body.get("shippingFee") != null ? body.get("shippingFee") : body.get("shipping_fee"));
        sMap.put("eta", body.get("eta"));
        sMap.put("currentStatus", "pending");
        sMap.put("createdAt", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(new java.util.Date()));

        return new Call<ApiResponse<Shipment>>() {
            @Override public Response<ApiResponse<Shipment>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Shipment>> callback) {
                dbRef.child("shipments").child(id).setValue(sMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbRef.child("shipments").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Shipment sh = mapSnapshotToShipment(snapshot);
                                callback.onResponse(null, Response.success(ApiResponse.success(sh, "success", null, null)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                            }
                        });
                    } else {
                        callback.onResponse(null, Response.success(ApiResponse.error("Failed to create shipment")));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<Shipment>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Shipment>> updateShipment(String id, Map<String, Object> body) {
        Map<String, Object> updates = new HashMap<>();
        if (body.containsKey("carrier")) updates.put("carrier", body.get("carrier"));
        if (body.containsKey("trackingNumber")) updates.put("trackingNumber", body.get("trackingNumber"));
        if (body.containsKey("shippingFee")) updates.put("shippingFee", body.get("shippingFee"));
        if (body.containsKey("eta")) updates.put("eta", body.get("eta"));
        if (body.containsKey("currentStatus")) updates.put("currentStatus", body.get("currentStatus"));
        if (body.containsKey("deliveredAt")) updates.put("deliveredAt", body.get("deliveredAt"));

        return new Call<ApiResponse<Shipment>>() {
            @Override public Response<ApiResponse<Shipment>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Shipment>> callback) {
                dbRef.child("shipments").child(id).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbRef.child("shipments").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Shipment sh = mapSnapshotToShipment(snapshot);
                                callback.onResponse(null, Response.success(ApiResponse.success(sh, "success", null, null)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                            }
                        });
                    } else {
                        callback.onResponse(null, Response.success(ApiResponse.error("Failed to update shipment")));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<Shipment>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }
}
