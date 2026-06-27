package vn.vuavuive.customer.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.request.RegisterRequest;
import vn.vuavuive.shared.util.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseUserRepository {

    private static class PendingRegistration {
        final RegisterRequest request;
        final String otpCode;

        PendingRegistration(RegisterRequest request, String otpCode) {
            this.request = request;
            this.otpCode = otpCode;
        }
    }

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference dbRef;
    private final SessionManager sessionManager;
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    @Inject
    public FirebaseUserRepository(SessionManager sessionManager) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.dbRef = FirebaseDatabase.getInstance().getReference();
        this.sessionManager = sessionManager;
    }

    // ── Phone Normalization ──────────────────────────────────────────────────
    public String normalizePhoneToEmail(String input) {
        if (input == null) return "";
        // Remove spaces, hyphens, parentheses
        String clean = input.replaceAll("[\\s\\-\\(\\)]", "");
        if (clean.startsWith("+84")) {
            clean = "0" + clean.substring(3);
        } else if (clean.startsWith("84") && clean.length() > 9) {
            clean = "0" + clean.substring(2);
        }

        // If it's already an email, return it. Otherwise, append domain
        if (clean.contains("@")) {
            return clean;
        }
        return clean + "@vuavuive.local";
    }

    // ── Helper to format dates to ISO 8601 ───────────────────────────────────
    private String getCurrentIsoString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    // ── Map Firebase Snapshot to User DTO ────────────────────────────────────
    private User mapSnapshotToUser(DataSnapshot s) {
        User u = new User();
        u.setId(s.child("id").getValue(String.class));
        u.setName(s.child("full_name").getValue(String.class));
        u.setPhone(s.child("phone").getValue(String.class));
        u.setEmail(s.child("email").getValue(String.class));
        u.setRole(s.child("role").getValue(String.class));
        u.setAddress(s.child("address").getValue(String.class));
        Boolean active = s.child("is_active").getValue(Boolean.class);
        u.setActive(active != null ? active : false);
        u.setCreatedAt(s.child("created_at").getValue(String.class));
        u.setUpdatedAt(s.child("updated_at").getValue(String.class));
        Integer points = s.child("points").getValue(Integer.class);
        u.setPoints(points != null ? points : 0);
        return u;
    }

    // ── Login ──────────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<User>> login(String phoneOrEmail, String password) {
        MutableLiveData<AuthRepository.Result<User>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        String email = normalizePhoneToEmail(phoneOrEmail);

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                String uid = firebaseAuth.getCurrentUser().getUid();
                // Read profile from Realtime Database
                dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = mapSnapshotToUser(snapshot);
                            sessionManager.saveUser(user);
                            result.postValue(AuthRepository.Result.success(user));
                        } else {
                            // Profile doesn't exist yet (fallback scenario)
                            createDefaultUserProfile(uid, email, phoneOrEmail, result);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        result.postValue(AuthRepository.Result.error("Lỗi đọc profile từ Firebase: " + error.getMessage()));
                    }
                });
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Sai mật khẩu hoặc tài khoản không tồn tại";
                result.postValue(AuthRepository.Result.error(errorMsg));
            }
        });

        return result;
    }

    // Create default user profile as fallback
    private void createDefaultUserProfile(String uid, String email, String phoneOrEmail, MutableLiveData<AuthRepository.Result<User>> result) {
        String phone = phoneOrEmail.contains("@") ? "" : phoneOrEmail;
        String now = getCurrentIsoString();

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", uid);
        profileData.put("full_name", "Khách Hàng Vui Vẻ");
        profileData.put("phone", phone);
        profileData.put("email", email);
        profileData.put("address", "");
        profileData.put("role", "CUSTOMER");
        profileData.put("points", 0);
        profileData.put("is_active", true);
        profileData.put("created_at", now);
        profileData.put("updated_at", now);

        dbRef.child("users").child(uid).setValue(profileData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = new User();
                user.setId(uid);
                user.setName("Khách Hàng Vui Vẻ");
                user.setPhone(phone);
                user.setEmail(email);
                user.setRole("CUSTOMER");
                user.setActive(true);
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                user.setPoints(0);

                sessionManager.saveUser(user);
                result.postValue(AuthRepository.Result.success(user));
            } else {
                result.postValue(AuthRepository.Result.error("Không thể khởi tạo profile mặc định"));
            }
        });
    }

    // ── Direct Register (for viewmodel compatibility) ───────────────────────
    public LiveData<AuthRepository.Result<User>> register(RegisterRequest request) {
        MutableLiveData<AuthRepository.Result<User>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());
        String email = normalizePhoneToEmail(request.getPhone());
        firebaseAuth.createUserWithEmailAndPassword(email, request.getPassword()).addOnCompleteListener(task -> {
            if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                String uid = firebaseAuth.getCurrentUser().getUid();
                createDefaultUserProfile(uid, email, request.getPhone(), result);
            } else {
                result.postValue(AuthRepository.Result.error(task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại"));
            }
        });
        return result;
    }

    // ── Register Flow (Resend Email OTP) ──────────────────────
    private static final String RESEND_API_KEY = "re_NyfhpxtP_GDQ3gqNeWEh3iFvuUgrJKefp"; // Configured from application-dev.yml

    public LiveData<AuthRepository.Result<Void>> sendRegisterOtp(RegisterRequest request) {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        if (request.getPhone() == null || request.getPhone().isEmpty()) {
            result.postValue(AuthRepository.Result.error("Số điện thoại không được để trống"));
            return result;
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            result.postValue(AuthRepository.Result.error("Email không được để trống"));
            return result;
        }

        // 1. Generate 6-digit random code
        String code = String.format(Locale.US, "%06d", (int) (Math.random() * 1000000));
        android.util.Log.d("FirebaseUserRepository", "Generated OTP: " + code + " for email: " + request.getEmail());

        // 2. Cache in memory
        pendingRegistrations.put(request.getPhone(), new PendingRegistration(request, code));

        // 3. Send Resend email in background thread
        new Thread(() -> {
            boolean isPlaceholderKey = "re_YOUR_RESEND_API_KEY".equals(RESEND_API_KEY) || RESEND_API_KEY.isEmpty();
            if (isPlaceholderKey) {
                android.util.Log.d("FirebaseUserRepository", "[RESEND FALLBACK] API Key is placeholder. OTP for email " + request.getEmail() + " is: " + code);
                result.postValue(AuthRepository.Result.success(null));
                return;
            }

            try {
                java.net.URL url = new java.net.URL("https://api.resend.com/emails");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + RESEND_API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Build json body
                String json = "{"
                        + "\"from\":\"Vua Vui Ve <onboarding@resend.dev>\","
                        + "\"to\":[\"" + request.getEmail() + "\"],"
                        + "\"subject\":\"[Vựa Vui Vẻ] Mã OTP đăng ký tài khoản\","
                        + "\"html\":\"<p>Chào bạn,</p><p>Mã OTP đăng ký tài khoản Vựa Vui Vẻ của bạn là: <strong>" + code + "</strong>. Hiệu lực trong vòng 5 phút.</p>\""
                        + "}";

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    android.util.Log.d("FirebaseUserRepository", "Resend OTP email sent successfully to: " + request.getEmail());
                    result.postValue(AuthRepository.Result.success(null));
                } else {
                    android.util.Log.e("FirebaseUserRepository", "Failed to send Resend email (HTTP " + responseCode + "). Falling back.");
                    // Fallback to success during development/testing
                    result.postValue(AuthRepository.Result.success(null));
                }
            } catch (Exception e) {
                android.util.Log.e("FirebaseUserRepository", "Lỗi gửi OTP qua Resend: " + e.getMessage() + ". Falling back.");
                // Fallback to success during development/testing
                result.postValue(AuthRepository.Result.success(null));
            }
        }).start();

        return result;
    }

    public LiveData<AuthRepository.Result<User>> verifyRegisterOtp(String phone, String code) {
        MutableLiveData<AuthRepository.Result<User>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        PendingRegistration pending = pendingRegistrations.get(phone);
        if (pending == null) {
            result.postValue(AuthRepository.Result.error("Yêu cầu đăng ký đã hết hạn. Vui lòng đăng ký lại."));
            return result;
        }

        if (!pending.otpCode.equals(code)) {
            result.postValue(AuthRepository.Result.error("Mã OTP không chính xác. Vui lòng kiểm tra lại."));
            return result;
        }

        RegisterRequest request = pending.request;
        String email = normalizePhoneToEmail(phone);

        firebaseAuth.createUserWithEmailAndPassword(email, request.getPassword()).addOnCompleteListener(task -> {
            if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                String uid = firebaseAuth.getCurrentUser().getUid();
                String now = getCurrentIsoString();

                Map<String, Object> profileData = new HashMap<>();
                profileData.put("id", uid);
                profileData.put("full_name", request.getName());
                profileData.put("phone", phone);
                profileData.put("email", email);
                profileData.put("address", request.getAddress() != null ? request.getAddress() : "");
                profileData.put("role", "CUSTOMER"); // Hardcoded to CUSTOMER
                profileData.put("points", 0);
                profileData.put("is_active", true);
                profileData.put("created_at", now);
                profileData.put("updated_at", now);

                dbRef.child("users").child(uid).setValue(profileData).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        User user = new User();
                        user.setId(uid);
                        user.setName(request.getName());
                        user.setPhone(phone);
                        user.setEmail(email);
                        user.setAddress(request.getAddress());
                        user.setRole("CUSTOMER");
                        user.setActive(true);
                        user.setCreatedAt(now);
                        user.setUpdatedAt(now);
                        user.setPoints(0);

                        sessionManager.saveUser(user);
                        pendingRegistrations.remove(phone);
                        result.postValue(AuthRepository.Result.success(user));
                    } else {
                        String dbError = dbTask.getException() != null ? dbTask.getException().getMessage() : "Lỗi ghi dữ liệu profile";
                        result.postValue(AuthRepository.Result.error(dbError));
                    }
                });
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Đăng ký tài khoản thất bại";
                result.postValue(AuthRepository.Result.error(errorMsg));
            }
        });

        return result;
    }

    // ── Check Session ───────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<User>> checkSession() {
        MutableLiveData<AuthRepository.Result<User>> result = new MutableLiveData<>();
        FirebaseUser fbUser = firebaseAuth.getCurrentUser();

        if (fbUser != null) {
            String uid = fbUser.getUid();
            dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = mapSnapshotToUser(snapshot);
                        sessionManager.saveUser(user);
                        result.postValue(AuthRepository.Result.success(user));
                    } else {
                        // User exists in Auth but not in RTDB, create default profile
                        createDefaultUserProfile(uid, fbUser.getEmail(), fbUser.getEmail(), result);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Fail gracefully by returning locally saved user
                    User localUser = sessionManager.getUser();
                    if (localUser != null) {
                        result.postValue(AuthRepository.Result.success(localUser));
                    } else {
                        result.postValue(AuthRepository.Result.error("Không thể đọc profile: " + error.getMessage()));
                    }
                }
            });
        } else {
            sessionManager.clearSession();
            result.postValue(AuthRepository.Result.error("Chưa đăng nhập"));
        }

        return result;
    }

    // ── Logout ─────────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> logout() {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        firebaseAuth.signOut();
        sessionManager.clearSession();
        result.postValue(AuthRepository.Result.success(null));
        return result;
    }

    // ── Forgot Password ────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<String>> forgotPassword(String phoneOrEmail) {
        MutableLiveData<AuthRepository.Result<String>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        String email = normalizePhoneToEmail(phoneOrEmail);
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                result.postValue(AuthRepository.Result.success("Yêu cầu đặt lại mật khẩu đã được gửi đến email của bạn."));
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Lỗi gửi yêu cầu";
                result.postValue(AuthRepository.Result.error(error));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Result<String>> verifyOtp(String phoneOrEmail, String otp) {
        MutableLiveData<AuthRepository.Result<String>> result = new MutableLiveData<>();
        // Mock verification code flow for reset password UI
        result.postValue(AuthRepository.Result.success("verified"));
        return result;
    }

    public LiveData<AuthRepository.Result<Void>> resetPassword(String resetToken, String newPassword) {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());
        // Since Firebase password reset is handled via reset email link, we mock this as success
        result.postValue(AuthRepository.Result.success(null));
        return result;
    }

    // ── Update Profile ─────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<User>> updateProfile(String name, String phone, String address) {
        MutableLiveData<AuthRepository.Result<User>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        FirebaseUser fbUser = firebaseAuth.getCurrentUser();
        if (fbUser == null) {
            result.postValue(AuthRepository.Result.error("Chưa đăng nhập"));
            return result;
        }

        String uid = fbUser.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("full_name", name);
        if (phone != null && !phone.isEmpty()) updates.put("phone", phone);
        if (address != null && !address.isEmpty()) updates.put("address", address);
        updates.put("updated_at", getCurrentIsoString());

        dbRef.child("users").child(uid).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = mapSnapshotToUser(snapshot);
                            sessionManager.saveUser(user);
                            result.postValue(AuthRepository.Result.success(user));
                        } else {
                            result.postValue(AuthRepository.Result.error("Không tìm thấy profile"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        result.postValue(AuthRepository.Result.error(error.getMessage()));
                    }
                });
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Cập nhật thất bại";
                result.postValue(AuthRepository.Result.error(error));
            }
        });

        return result;
    }

    // ── Change Password ────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> changePassword(String oldPassword, String newPassword) {
        MutableLiveData<AuthRepository.Result<Void>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        FirebaseUser fbUser = firebaseAuth.getCurrentUser();
        if (fbUser == null) {
            result.postValue(AuthRepository.Result.error("Chưa đăng nhập"));
            return result;
        }

        fbUser.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                result.postValue(AuthRepository.Result.success(null));
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Đổi mật khẩu thất bại";
                result.postValue(AuthRepository.Result.error(error));
            }
        });

        return result;
    }

    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null && sessionManager.isLoggedIn();
    }
}
