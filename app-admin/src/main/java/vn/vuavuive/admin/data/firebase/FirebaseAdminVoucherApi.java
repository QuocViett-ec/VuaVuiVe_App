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
import vn.vuavuive.shared.data.api.AdminVoucherApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Voucher;

public class FirebaseAdminVoucherApi implements AdminVoucherApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    private Voucher mapSnapshotToVoucher(DataSnapshot s) {
        Voucher v = new Voucher();
        v.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
        v.setCode(s.child("code").getValue(String.class));
        v.setType(s.child("type").getValue(String.class));
        
        Double val = s.child("value").getValue(Double.class);
        v.setValue(val != null ? val : 0.0);
        
        Double cap = s.child("cap").getValue(Double.class);
        v.setCap(cap != null ? cap : 0.0);
        
        Double minOrder = s.child("minOrderValue").getValue(Double.class);
        if (minOrder == null) minOrder = s.child("min_order_value").getValue(Double.class);
        v.setMinOrderValue(minOrder != null ? minOrder : 0.0);
        
        Integer maxUses = s.child("maxUses").getValue(Integer.class);
        if (maxUses == null) maxUses = s.child("max_uses").getValue(Integer.class);
        v.setMaxUses(maxUses != null ? maxUses : 0);
        
        v.setStartsAt(s.child("startsAt").getValue(String.class) != null ? s.child("startsAt").getValue(String.class) : s.child("starts_at").getValue(String.class));
        v.setExpiresAt(s.child("expiresAt").getValue(String.class) != null ? s.child("expiresAt").getValue(String.class) : s.child("expires_at").getValue(String.class));
        
        Boolean active = s.child("isActive").getValue(Boolean.class);
        if (active == null) active = s.child("is_active").getValue(Boolean.class);
        v.setActive(active != null ? active : true);
        
        v.setNote(s.child("note").getValue(String.class));
        return v;
    }

    @Override
    public Call<ApiResponse<List<Voucher>>> getVouchers() {
        return new Call<ApiResponse<List<Voucher>>>() {
            @Override public Response<ApiResponse<List<Voucher>>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<List<Voucher>>> callback) {
                dbRef.child("vouchers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Voucher> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Voucher v = mapSnapshotToVoucher(s);
                            list.add(v);
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
            @NonNull @Override public Call<ApiResponse<List<Voucher>>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Voucher>> createVoucher(Map<String, Object> body) {
        String code = ((String) body.get("code")).toUpperCase();
        String id = "vouch-" + UUID.randomUUID().toString();

        Map<String, Object> vMap = new HashMap<>();
        vMap.put("id", id);
        vMap.put("code", code);
        vMap.put("type", body.get("type"));
        vMap.put("value", body.get("value"));
        vMap.put("cap", body.get("cap"));
        vMap.put("minOrderValue", body.get("minOrderValue"));
        vMap.put("maxUses", body.get("maxUses"));
        vMap.put("usedCount", 0);
        vMap.put("startsAt", body.get("startsAt"));
        vMap.put("expiresAt", body.get("expiresAt"));
        vMap.put("isActive", true);
        vMap.put("is_active", true);
        vMap.put("note", body.get("note"));

        return new Call<ApiResponse<Voucher>>() {
            @Override public Response<ApiResponse<Voucher>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Voucher>> callback) {
                dbRef.child("vouchers").child(code).setValue(vMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbRef.child("vouchers").child(code).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Voucher v = mapSnapshotToVoucher(snapshot);
                                callback.onResponse(null, Response.success(ApiResponse.success(v, "success", null, null)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                            }
                        });
                    } else {
                        callback.onResponse(null, Response.success(ApiResponse.error("Failed to create voucher")));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<Voucher>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Voucher>> updateVoucher(String code, Map<String, Object> body) {
        String targetCode = code.toUpperCase();
        Map<String, Object> updates = new HashMap<>();
        if (body.containsKey("type")) updates.put("type", body.get("type"));
        if (body.containsKey("value")) updates.put("value", body.get("value"));
        if (body.containsKey("cap")) updates.put("cap", body.get("cap"));
        if (body.containsKey("minOrderValue")) updates.put("minOrderValue", body.get("minOrderValue"));
        if (body.containsKey("maxUses")) updates.put("maxUses", body.get("maxUses"));
        if (body.containsKey("startsAt")) updates.put("startsAt", body.get("startsAt"));
        if (body.containsKey("expiresAt")) updates.put("expiresAt", body.get("expiresAt"));
        if (body.containsKey("isActive")) {
            updates.put("isActive", body.get("isActive"));
            updates.put("is_active", body.get("isActive"));
        }
        if (body.containsKey("note")) updates.put("note", body.get("note"));

        return new Call<ApiResponse<Voucher>>() {
            @Override public Response<ApiResponse<Voucher>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Voucher>> callback) {
                dbRef.child("vouchers").child(targetCode).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbRef.child("vouchers").child(targetCode).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Voucher v = mapSnapshotToVoucher(snapshot);
                                callback.onResponse(null, Response.success(ApiResponse.success(v, "success", null, null)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                            }
                        });
                    } else {
                        callback.onResponse(null, Response.success(ApiResponse.error("Failed to update voucher")));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<Voucher>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Void>> deleteVoucher(String code) {
        String targetCode = code.toUpperCase();
        return new Call<ApiResponse<Void>>() {
            @Override public Response<ApiResponse<Void>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Void>> callback) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("isActive", false);
                updates.put("is_active", false);
                dbRef.child("vouchers").child(targetCode).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResponse(null, Response.success(ApiResponse.success(null, "success", null, null)));
                    } else {
                        callback.onResponse(null, Response.success(ApiResponse.error("Failed to delete voucher")));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<Void>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }
}
