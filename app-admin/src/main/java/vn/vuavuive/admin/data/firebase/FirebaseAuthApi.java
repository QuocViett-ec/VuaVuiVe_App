package vn.vuavuive.admin.data.firebase;

import androidx.annotation.NonNull;
import android.os.Handler;
import android.os.Looper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import vn.vuavuive.shared.data.api.AuthApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.request.GoogleLoginRequest;
import vn.vuavuive.shared.data.dto.request.LoginRequest;
import vn.vuavuive.shared.data.dto.request.RegisterRequest;

public class FirebaseAuthApi implements AuthApi {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    // Whitelist for admin/staff/audit roles
    private static final Map<String, String> ROLE_WHITELIST = new HashMap<>();
    static {
        ROLE_WHITELIST.put("admin@vuavuive.vn", "ADMIN");
        ROLE_WHITELIST.put("staff@vuavuive.vn", "STAFF");
        ROLE_WHITELIST.put("audit@vuavuive.vn", "AUDIT");
        ROLE_WHITELIST.put("admin@vuavuive.com", "ADMIN");
    }

    private static final Map<String, String> NAME_MAP = new HashMap<>();
    static {
        NAME_MAP.put("admin@vuavuive.vn", "Admin Quản trị");
        NAME_MAP.put("staff@vuavuive.vn", "Nhân viên Vận hành");
        NAME_MAP.put("audit@vuavuive.vn", "Kiểm toán viên");
        NAME_MAP.put("admin@vuavuive.com", "Admin Quản trị");
    }

    @Override
    public Call<ApiResponse<User>> adminLogin(@Body LoginRequest body) {
        String email = body.getIdentifier();
        String password = body.getPassword();

        // 1. Authenticate with Firebase Auth
        try {
            // Retrofit calls are expected to be asynchronous or synchronous.
            // Since this will be executed/enqueued, we will return a FirebaseCall.
            // However, Firebase Auth tasks are async. To implement this properly synchronously
            // or inside our wrapper, we will use a custom Call that waits for task completion
            // or simply perform the Firebase task and complete the callback.
            // Since we need to return Call immediately, we will do the task asynchronously when enqueued,
            // or synchronously when executed.
            return new Call<ApiResponse<User>>() {
                @Override
                public Response<ApiResponse<User>> execute() {
                    throw new UnsupportedOperationException("Synchronous login not supported");
                }

                @Override
                public void enqueue(@NonNull retrofit2.Callback<ApiResponse<User>> callback) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().getUser() != null) {
                                FirebaseUser fUser = task.getResult().getUser();
                                String uid = fUser.getUid();
                                fetchOrInitializeUserProfile(uid, fUser.getEmail(), callback);
                            } else {
                                String lowerEmail = email != null ? email.toLowerCase(java.util.Locale.ROOT) : "";
                                if (ROLE_WHITELIST.containsKey(lowerEmail)) {
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(createTask -> {
                                            if (createTask.isSuccessful() && createTask.getResult().getUser() != null) {
                                                FirebaseUser fUser = createTask.getResult().getUser();
                                                fetchOrInitializeUserProfile(fUser.getUid(), fUser.getEmail(), callback);
                                            } else {
                                                Exception e = task.getException();
                                                String errMsg = e != null ? e.getMessage() : "Đăng nhập thất bại";
                                                callback.onResponse(this, retrofit2.Response.success(
                                                        ApiResponse.error(errMsg)
                                                ));
                                            }
                                        });
                                } else {
                                    Exception e = task.getException();
                                    String errMsg = e != null ? e.getMessage() : "Đăng nhập thất bại";
                                    callback.onResponse(this, retrofit2.Response.success(
                                            ApiResponse.error(errMsg)
                                    ));
                                }
                            }
                        });
                }

                @Override public boolean isExecuted() { return false; }
                @Override public void cancel() {}
                @Override public boolean isCanceled() { return false; }
                @NonNull
                @Override public Call<ApiResponse<User>> clone() { return this; }
                @NonNull
                @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
                @NonNull
                @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
            };
        } catch (Exception e) {
            return new FirebaseCall<>(e);
        }
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private void fetchOrInitializeUserProfile(String uid, String email, @NonNull retrofit2.Callback<ApiResponse<User>> callback) {
        dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = mapSnapshotToUser(snapshot, uid);
                    if (!user.isBackoffice()) {
                        auth.signOut();
                        MAIN.post(() -> callback.onResponse(null, retrofit2.Response.success(
                            ApiResponse.error("Tài khoản không có quyền truy cập trang quản trị")
                        )));
                        return;
                    }
                    // Ensure role matches whitelist if email is in whitelist
                    String expectedRole = ROLE_WHITELIST.get(email != null ? email.toLowerCase() : "");
                    if (expectedRole != null && !expectedRole.equalsIgnoreCase(user.getRole())) {
                        user.setRole(expectedRole);
                        dbRef.child("users").child(uid).child("role").setValue(expectedRole.toUpperCase());
                    }
                    respondWithFirebaseToken(auth.getCurrentUser(), user, callback);
                } else {
                    // Check whitelist
                    String emailKey = email != null ? email.toLowerCase() : "";
                    String role = ROLE_WHITELIST.get(emailKey);
                    if (role != null) {
                        // Initialize user profile
                        User user = new User();
                        user.setId(uid);
                        user.setEmail(email);
                        user.setName(NAME_MAP.getOrDefault(emailKey, "Admin"));
                        user.setRole(role);
                        user.setActive(true);
                        user.setCreatedAt(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(new java.util.Date()));
                        user.setProvider("local");

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", uid);
                        userMap.put("full_name", user.getName());
                        userMap.put("name", user.getName());
                        userMap.put("email", user.getEmail());
                        userMap.put("role", role.toUpperCase());
                        userMap.put("isActive", true);
                        userMap.put("is_active", true);
                        userMap.put("created_at", user.getCreatedAt());
                        userMap.put("provider", "local");

                        dbRef.child("users").child(uid).setValue(userMap)
                            .addOnCompleteListener(writeTask -> {
                                if (writeTask.isSuccessful()) {
                                    respondWithFirebaseToken(auth.getCurrentUser(), user, callback);
                                } else {
                                    MAIN.post(() -> callback.onResponse(null, retrofit2.Response.success(
                                        ApiResponse.error("Không thể khởi tạo thông tin người dùng")
                                    )));
                                }
                            });
                    } else {
                        // User not allowed to login as admin
                        auth.signOut();
                        MAIN.post(() -> callback.onResponse(null, retrofit2.Response.success(
                            ApiResponse.error("Tài khoản không có quyền truy cập trang quản trị")
                        )));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                MAIN.post(() -> callback.onResponse(null, retrofit2.Response.success(
                    ApiResponse.error("Lỗi cơ sở dữ liệu: " + error.getMessage())
                )));
            }
        });
    }

    private User mapSnapshotToUser(DataSnapshot s, String uid) {
        User u = new User();
        u.setId(uid);
        u.setEmail(s.child("email").getValue(String.class));
        u.setName(s.child("name").getValue(String.class));
        u.setPhone(s.child("phone").getValue(String.class));
        u.setAddress(s.child("address").getValue(String.class));
        
        String role = s.child("role").getValue(String.class);
        u.setRole(role != null ? role.toUpperCase() : "CUSTOMER");
        
        Boolean active = s.child("isActive").getValue(Boolean.class);
        if (active == null) active = s.child("is_active").getValue(Boolean.class);
        u.setActive(active != null ? active : true);
        
        u.setCreatedAt(s.child("created_at").getValue(String.class));
        u.setProvider(s.child("provider").getValue(String.class));
        return u;
    }

    // ── Unused AuthApi Methods ──────────────────────────────────────────────

    @Override
    public Call<ApiResponse<User>> register(@Body RegisterRequest body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Void>> sendRegisterOtp(@Body RegisterRequest body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<User>> verifyRegisterOtp(@Body Map<String, String> body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<User>> login(@Body LoginRequest body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<User>> shipperLogin(@Body LoginRequest body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<User>> googleLogin(@Body GoogleLoginRequest body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Void>> logout() {
        auth.signOut();
        return new FirebaseCall<>(ApiResponse.success(null, "Đăng xuất thành công", null, null));
    }

    @Override
    public Call<ApiResponse<User>> getMe() {
        FirebaseUser fUser = auth.getCurrentUser();
        if (fUser == null) {
            return new FirebaseCall<>(ApiResponse.error("Chưa đăng nhập"));
        }
        return new Call<ApiResponse<User>>() {
            @Override public Response<ApiResponse<User>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull retrofit2.Callback<ApiResponse<User>> callback) {
                fetchOrInitializeUserProfile(fUser.getUid(), fUser.getEmail(), callback);
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
    public Call<ApiResponse<User>> updateProfile(@Body Map<String, Object> body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Void>> changePassword(@Body Map<String, String> body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Void>> setLocalPassword(@Body Map<String, String> body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Void>> forgotPassword(@Body Map<String, String> body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Void>> verifyOtp(@Body Map<String, String> body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Call<ApiResponse<Void>> resetPassword(@Body Map<String, String> body) {
        throw new UnsupportedOperationException();
    }

    private void respondWithFirebaseToken(
            FirebaseUser firebaseUser,
            User user,
            @NonNull retrofit2.Callback<ApiResponse<User>> callback) {
        if (firebaseUser == null) {
            MAIN.post(() -> callback.onResponse(null,
                    retrofit2.Response.success(ApiResponse.error("Không lấy được phiên Firebase"))));
            return;
        }
        firebaseUser.getIdToken(false)
                .addOnSuccessListener(result -> {
                    ApiResponse<User> response = ApiResponse.success(
                            user, "success", result.getToken(), null);
                    MAIN.post(() -> callback.onResponse(null, retrofit2.Response.success(response)));
                })
                .addOnFailureListener(error -> MAIN.post(() -> callback.onResponse(null,
                        retrofit2.Response.success(ApiResponse.error("Không lấy được token đăng nhập")))));
    }
}
