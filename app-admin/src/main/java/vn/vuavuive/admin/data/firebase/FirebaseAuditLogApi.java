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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.AuditLogApi;
import vn.vuavuive.shared.data.dto.ApiResponse;

public class FirebaseAuditLogApi implements AuditLogApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    @Override
    public Call<ApiResponse<List<Map<String, Object>>>> getAuditLogs(
            int page, int limit, String action, String adminId, String from, String to) {
        return new Call<ApiResponse<List<Map<String, Object>>>>() {
            @Override public Response<ApiResponse<List<Map<String, Object>>>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<List<Map<String, Object>>>> callback) {
                dbRef.child("auditLogs").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Map<String, Object>> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Map<String, Object> log = new HashMap<>();
                            log.put("id", s.getKey());
                            for (DataSnapshot child : s.getChildren()) {
                                log.put(child.getKey(), child.getValue());
                            }
                            list.add(log);
                        }

                        // Filter by action
                        if (action != null && !action.trim().isEmpty() && !"all".equalsIgnoreCase(action)) {
                            List<Map<String, Object>> filtered = new ArrayList<>();
                            for (Map<String, Object> log : list) {
                                String logAction = (String) log.get("action");
                                if (action.equalsIgnoreCase(logAction)) {
                                    filtered.add(log);
                                }
                            }
                            list = filtered;
                        }

                        // Filter by adminId
                        if (adminId != null && !adminId.trim().isEmpty() && !"all".equalsIgnoreCase(adminId)) {
                            List<Map<String, Object>> filtered = new ArrayList<>();
                            for (Map<String, Object> log : list) {
                                String logAdminId = (String) log.get("adminId");
                                if (adminId.equalsIgnoreCase(logAdminId)) {
                                    filtered.add(log);
                                }
                            }
                            list = filtered;
                        }

                        // Paginate
                        int startIndex = (page - 1) * limit;
                        List<Map<String, Object>> paginated = new ArrayList<>();
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
            @NonNull @Override public Call<ApiResponse<List<Map<String, Object>>>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Void>> createAuditLog(Map<String, Object> body) {
        String id = dbRef.child("auditLogs").push().getKey();
        if (id == null) {
            id = "log-" + System.currentTimeMillis();
        }
        Map<String, Object> logMap = new HashMap<>(body);
        logMap.put("id", id);
        if (!logMap.containsKey("createdAt") && !logMap.containsKey("created_at")) {
            logMap.put("createdAt", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(new java.util.Date()));
        }

        final String finalId = id;
        return new Call<ApiResponse<Void>>() {
            @Override public Response<ApiResponse<Void>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Void>> callback) {
                dbRef.child("auditLogs").child(finalId).setValue(logMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResponse(null, Response.success(ApiResponse.success(null, "success", null, null)));
                    } else {
                        callback.onResponse(null, Response.success(ApiResponse.error("Failed to create audit log")));
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
