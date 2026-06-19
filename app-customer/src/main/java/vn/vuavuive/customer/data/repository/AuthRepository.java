package vn.vuavuive.customer.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.AuthApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.request.LoginRequest;
import vn.vuavuive.shared.data.dto.request.RegisterRequest;
import vn.vuavuive.shared.util.SessionManager;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepository {

    private final AuthApi authApi;
    private final SessionManager sessionManager;

    @Inject
    public AuthRepository(AuthApi authApi, SessionManager sessionManager) {
        this.authApi = authApi;
        this.sessionManager = sessionManager;
    }

    // ── Login ──────────────────────────────────────────────────────────────
    public LiveData<Result<User>> login(String phoneOrEmail, String password) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        result.postValue(Result.loading());

        // Determine if input is email or phone
        LoginRequest request = new LoginRequest(phoneOrEmail, password);

        authApi.login(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiResponse<User> body = response.body();
                    User user = body.getData();
                    sessionManager.saveUser(user);
                    sessionManager.saveTokens(body.getAccessToken(), body.getRefreshToken());
                    result.postValue(Result.success(user));
                } else {
                    String msg = extractError(response);
                    result.postValue(Result.error(msg));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                result.postValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Register ───────────────────────────────────────────────────────────
    public LiveData<Result<User>> register(RegisterRequest request) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        result.postValue(Result.loading());

        authApi.register(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiResponse<User> body = response.body();
                    User user = body.getData();
                    sessionManager.saveUser(user);
                    sessionManager.saveTokens(body.getAccessToken(), body.getRefreshToken());
                    result.postValue(Result.success(user));
                } else {
                    result.postValue(Result.error(extractError(response)));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                result.postValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Send Register OTP ──────────────────────────────────────────────────
    public LiveData<Result<Void>> sendRegisterOtp(RegisterRequest request) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.postValue(Result.loading());

        authApi.sendRegisterOtp(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(Result.success(null));
                } else {
                    result.postValue(Result.error(extractError(response)));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.postValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Verify Register OTP ────────────────────────────────────────────────
    public LiveData<Result<User>> verifyRegisterOtp(String phone, String code) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        result.postValue(Result.loading());

        Map<String, String> body = new HashMap<>();
        body.put("phone", phone);
        body.put("code", code);

        authApi.verifyRegisterOtp(body).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiResponse<User> apiResponse = response.body();
                    User user = apiResponse.getData();
                    sessionManager.saveUser(user);
                    sessionManager.saveTokens(apiResponse.getAccessToken(), apiResponse.getRefreshToken());
                    result.postValue(Result.success(user));
                } else {
                    result.postValue(Result.error(extractError(response)));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                result.postValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Check session (GET /api/auth/me) ───────────────────────────────────
    public LiveData<Result<User>> checkSession() {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        authApi.getMe().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = response.body().getData();
                    sessionManager.saveUser(user);
                    result.postValue(Result.success(user));
                } else {
                    sessionManager.clearSession();
                    result.postValue(Result.error("Chưa đăng nhập"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                result.postValue(Result.error(t.getMessage()));
            }
        });
        return result;
    }

    // ── Logout ─────────────────────────────────────────────────────────────
    public LiveData<Result<Void>> logout() {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        authApi.logout().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                sessionManager.clearSession();
                result.postValue(Result.success(null));
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                sessionManager.clearSession();
                result.postValue(Result.success(null)); // Logout locally anyway
            }
        });
        return result;
    }

    // ── Forgot Password ────────────────────────────────────────────────────
    public LiveData<Result<String>> forgotPassword(String phoneOrEmail) {
        MutableLiveData<Result<String>> result = new MutableLiveData<>();
        result.postValue(Result.loading());

        Map<String, String> body = new HashMap<>();
        if (phoneOrEmail.contains("@")) {
            body.put("email", phoneOrEmail);
        } else {
            body.put("phone", phoneOrEmail);
        }

        authApi.forgotPassword(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    result.postValue(Result.success("OTP đã được gửi"));
                } else {
                    result.postValue(Result.error(extractError(response)));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.postValue(Result.error("Lỗi kết nối"));
            }
        });
        return result;
    }

    // ── Verify OTP ─────────────────────────────────────────────────────────
    public LiveData<Result<String>> verifyOtp(String phoneOrEmail, String otp) {
        MutableLiveData<Result<String>> result = new MutableLiveData<>();
        result.postValue(Result.loading());

        Map<String, String> body = new HashMap<>();
        body.put("otp", otp);
        if (phoneOrEmail.contains("@")) {
            body.put("email", phoneOrEmail);
        } else {
            body.put("phone", phoneOrEmail);
        }

        authApi.verifyOtp(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Backend trả resetToken trong message hoặc data
                    result.postValue(Result.success("verified"));
                } else {
                    result.postValue(Result.error(extractError(response)));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.postValue(Result.error("Lỗi kết nối"));
            }
        });
        return result;
    }

    // ── Reset Password ─────────────────────────────────────────────────────
    public LiveData<Result<Void>> resetPassword(String resetToken, String newPassword) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.postValue(Result.loading());

        Map<String, String> body = new HashMap<>();
        body.put("resetToken", resetToken);
        body.put("newPassword", newPassword);

        authApi.resetPassword(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    result.postValue(Result.success(null));
                } else {
                    result.postValue(Result.error(extractError(response)));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.postValue(Result.error("Lỗi kết nối"));
            }
        });
        return result;
    }

    // ── Update Profile ─────────────────────────────────────────────────────
    public LiveData<Result<User>> updateProfile(String name, String phone, String address) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        result.postValue(Result.loading());

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        if (phone != null && !phone.isEmpty()) body.put("phone", phone);
        if (address != null && !address.isEmpty()) body.put("address", address);

        authApi.updateProfile(body).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = response.body().getData();
                    sessionManager.saveUser(user);
                    result.postValue(Result.success(user));
                } else {
                    result.postValue(Result.error(extractError(response)));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                result.postValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Change Password ────────────────────────────────────────────────────
    public LiveData<Result<Void>> changePassword(String oldPassword, String newPassword) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.postValue(Result.loading());

        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);

        authApi.changePassword(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    result.postValue(Result.success(null));
                } else {
                    result.postValue(Result.error(extractError(response)));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.postValue(Result.error("Lỗi kết nối: " + t.getMessage()));
            }
        });
        return result;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    private <T> String extractError(Response<T> response) {
        try {
            if (response.errorBody() != null) {
                String errorStr = response.errorBody().string();
                // Try to parse JSON error message
                if (errorStr.contains("\"message\"")) {
                    int start = errorStr.indexOf("\"message\"") + 11;
                    int end = errorStr.indexOf("\"", start + 1);
                    if (end > start) return errorStr.substring(start, end);
                }
                return errorStr;
            }
        } catch (Exception ignored) {}

        switch (response.code()) {
            case 400: return "Thông tin không hợp lệ";
            case 401: return "Sai tên đăng nhập hoặc mật khẩu";
            case 403: return "Tài khoản bị vô hiệu hóa";
            case 409: return "Số điện thoại hoặc email đã được sử dụng";
            case 500: return "Lỗi máy chủ, vui lòng thử lại";
            default:  return "Lỗi không xác định (HTTP " + response.code() + ")";
        }
    }

    // ── Inner Result class ─────────────────────────────────────────────────
    public static class Result<T> {
        public enum Status { LOADING, SUCCESS, ERROR }
        public final Status status;
        public final T data;
        public final String message;

        private Result(Status status, T data, String message) {
            this.status = status;
            this.data = data;
            this.message = message;
        }

        public static <T> Result<T> loading() { return new Result<>(Status.LOADING, null, null); }
        public static <T> Result<T> success(T data) { return new Result<>(Status.SUCCESS, data, null); }
        public static <T> Result<T> error(String msg) { return new Result<>(Status.ERROR, null, msg); }
    }
}
