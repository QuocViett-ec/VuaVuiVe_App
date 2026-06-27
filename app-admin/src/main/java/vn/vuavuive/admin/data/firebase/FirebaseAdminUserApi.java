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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.AdminUserApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.User;

public class FirebaseAdminUserApi implements AdminUserApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    private User mapSnapshotToUser(DataSnapshot s) {
        User u = new User();
        u.setId(s.getKey());
        
        String name = s.child("name").getValue(String.class);
        if (name == null || name.isEmpty()) {
            name = s.child("full_name").getValue(String.class);
        }
        u.setName(name);
        
        u.setPhone(s.child("phone").getValue(String.class));
        u.setEmail(s.child("email").getValue(String.class));
        u.setAvatar(s.child("avatar").getValue(String.class));
        u.setProvider(s.child("provider").getValue(String.class));
        u.setAddress(s.child("address").getValue(String.class));
        u.setRole(s.child("role").getValue(String.class));
        
        Boolean active = s.child("is_active").getValue(Boolean.class);
        if (active == null) active = s.child("isActive").getValue(Boolean.class);
        u.setActive(active != null ? active : true);
        
        u.setCreatedAt(s.child("created_at").getValue(String.class));
        u.setUpdatedAt(s.child("updated_at").getValue(String.class));
        
        Integer pts = s.child("points").getValue(Integer.class);
        u.setPoints(pts != null ? pts : 0);
        
        return u;
    }

    @Override
    public Call<ApiResponse<List<User>>> getUsers(int page, int limit, String search, String role) {
        return new Call<ApiResponse<List<User>>>() {
            @Override public Response<ApiResponse<List<User>>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<List<User>>> callback) {
                dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<User> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            User u = mapSnapshotToUser(s);
                            list.add(u);
                        }

                        // Filter by role
                        if (role != null && !role.trim().isEmpty() && !"all".equalsIgnoreCase(role)) {
                            List<User> filtered = new ArrayList<>();
                            for (User u : list) {
                                if (role.equalsIgnoreCase(u.getRole())) {
                                    filtered.add(u);
                                }
                            }
                            list = filtered;
                        }

                        // Filter by search
                        if (search != null && !search.trim().isEmpty()) {
                            String query = search.trim().toLowerCase();
                            List<User> filtered = new ArrayList<>();
                            for (User u : list) {
                                if ((u.getName() != null && u.getName().toLowerCase().contains(query)) ||
                                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(query)) ||
                                    (u.getPhone() != null && u.getPhone().contains(query))) {
                                    filtered.add(u);
                                }
                            }
                            list = filtered;
                        }

                        // Paginate
                        int startIndex = (page - 1) * limit;
                        List<User> paginated = new ArrayList<>();
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
            @NonNull @Override public Call<ApiResponse<List<User>>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<User>> getUser(String id) {
        return new Call<ApiResponse<User>>() {
            @Override public Response<ApiResponse<User>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<User>> callback) {
                dbRef.child("users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onResponse(null, Response.success(ApiResponse.error("User not found")));
                            return;
                        }
                        User u = mapSnapshotToUser(snapshot);
                        callback.onResponse(null, Response.success(ApiResponse.success(u, "success", null, null)));
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
            @NonNull @Override public Call<ApiResponse<User>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<User>> updateUser(String id, Map<String, Object> body) {
        Map<String, Object> updates = new HashMap<>();
        if (body.containsKey("name")) {
            updates.put("name", body.get("name"));
            updates.put("full_name", body.get("name"));
        }
        if (body.containsKey("phone")) updates.put("phone", body.get("phone"));
        if (body.containsKey("email")) updates.put("email", body.get("email"));
        if (body.containsKey("address")) updates.put("address", body.get("address"));
        if (body.containsKey("role")) updates.put("role", ((String) body.get("role")).toUpperCase());
        if (body.containsKey("isActive")) {
            updates.put("is_active", body.get("isActive"));
            updates.put("isActive", body.get("isActive"));
        }
        
        updates.put("updated_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(new java.util.Date()));

        return new Call<ApiResponse<User>>() {
            @Override public Response<ApiResponse<User>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<User>> callback) {
                dbRef.child("users").child(id).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbRef.child("users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User u = mapSnapshotToUser(snapshot);
                                callback.onResponse(null, Response.success(ApiResponse.success(u, "success", null, null)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                            }
                        });
                    } else {
                        callback.onResponse(null, Response.success(ApiResponse.error("Failed to update user in Firebase")));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<User>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Void>> deleteUser(String id) {
        return new Call<ApiResponse<Void>>() {
            @Override public Response<ApiResponse<Void>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<Void>> callback) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("is_active", false);
                updates.put("isActive", false);
                dbRef.child("users").child(id).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResponse(null, Response.success(ApiResponse.success(null, "success", null, null)));
                    } else {
                        callback.onResponse(null, Response.success(ApiResponse.error("Failed to delete user")));
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

    @Override
    public Call<ResponseBody> exportUsers() {
        return new FirebaseCall<>(ResponseBody.create("id,email,role\n", okhttp3.MediaType.parse("text/csv")));
    }
}
