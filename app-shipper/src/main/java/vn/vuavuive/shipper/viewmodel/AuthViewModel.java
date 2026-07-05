package vn.vuavuive.shipper.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;
import vn.vuavuive.shipper.data.repository.FirebaseShipperRepository;
import vn.vuavuive.shared.data.dto.User;

/**
 * AuthViewModel — Xử lý đăng nhập Firebase Auth cho Shipper.
 */
@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final FirebaseShipperRepository repository;

    @Inject
    public AuthViewModel(FirebaseShipperRepository repository) {
        this.repository = repository;
    }

    public LiveData<Result<User>> login(String email, String password) {
        // Delegate to Firebase repository, map Result type
        return mapResult(repository.login(email, password));
    }

    public boolean isLoggedIn() {
        return repository.isLoggedIn();
    }

    public void logout() {
        repository.logout();
    }

    /**
     * Map FirebaseShipperRepository.Result → AuthViewModel.Result
     * (giữ nguyên inner Result class để UI không cần thay đổi)
     */
    private <T> LiveData<Result<T>> mapResult(LiveData<FirebaseShipperRepository.Result<T>> source) {
        MediatorLiveData<Result<T>> out = new MediatorLiveData<>();
        out.addSource(source, r -> {
            if (r == null) return;
            switch (r.status) {
                case LOADING:  out.postValue(Result.loading());         break;
                case SUCCESS:  out.postValue(Result.success(r.data));   break;
                case ERROR:    out.postValue(Result.error(r.message));  break;
            }
            if (r.status != FirebaseShipperRepository.Result.Status.LOADING) {
                out.removeSource(source);
            }
        });
        return out;
    }

    // ─── Inner Result class (UI code không cần thay đổi) ────────────────────
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

