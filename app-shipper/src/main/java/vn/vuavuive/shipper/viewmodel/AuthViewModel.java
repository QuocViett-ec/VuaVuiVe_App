package vn.vuavuive.shipper.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.AuthApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.request.LoginRequest;
import vn.vuavuive.shared.util.SessionManager;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final AuthApi authApi;
    private final SessionManager sessionManager;

    @Inject
    public AuthViewModel(AuthApi authApi, SessionManager sessionManager) {
        this.authApi = authApi;
        this.sessionManager = sessionManager;
    }

    public LiveData<Result<User>> login(String identifier, String password) {
        MutableLiveData<Result<User>> out = new MutableLiveData<>();
        out.setValue(Result.loading());
        authApi.shipperLogin(new LoginRequest(identifier, password)).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                ApiResponse<User> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    sessionManager.saveUser(body.getData());
                    sessionManager.saveTokens(body.getAccessToken(), body.getRefreshToken());
                    out.setValue(Result.success(body.getData()));
                } else {
                    out.setValue(Result.error(body != null ? body.getMessage() : "Dang nhap that bai"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                out.setValue(Result.error(t.getMessage()));
            }
        });
        return out;
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn() && sessionManager.isShipper() && sessionManager.hasValidAccessToken();
    }

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

        static <T> Result<T> loading() { return new Result<>(Status.LOADING, null, null); }
        static <T> Result<T> success(T data) { return new Result<>(Status.SUCCESS, data, null); }
        static <T> Result<T> error(String message) { return new Result<>(Status.ERROR, null, message); }
    }
}
