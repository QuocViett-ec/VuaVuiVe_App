package vn.vuavuive.admin.data.firebase;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.ShipmentApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Shipment;

public class FirebaseShipmentApi implements ShipmentApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

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
    public Call<ApiResponse<List<Shipment>>> getMyShipments() {
        return new Call<ApiResponse<List<Shipment>>>() {
            @Override public Response<ApiResponse<List<Shipment>>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<List<Shipment>>> callback) {
                String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
                dbRef.child("shipments").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Shipment> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Shipment sh = mapSnapshotToShipment(s);
                            if (uid.isEmpty() || uid.equals(sh.getCustomerId())) {
                                list.add(sh);
                            }
                        }
                        callback.onResponse(null, Response.success(ApiResponse.success(list, "success", null, null)));
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
    public Call<ApiResponse<Shipment>> getShipment(String id) {
        return new Call<ApiResponse<Shipment>>() {
            @Override public Response<ApiResponse<Shipment>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Shipment>> callback) {
                dbRef.child("shipments").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onResponse(null, Response.success(ApiResponse.error("Shipment not found")));
                            return;
                        }
                        Shipment sh = mapSnapshotToShipment(snapshot);
                        callback.onResponse(null, Response.success(ApiResponse.success(sh, "success", null, null)));
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
            @NonNull @Override public Call<ApiResponse<Shipment>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }
}
