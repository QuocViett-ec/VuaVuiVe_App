package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.request.RegisterRequest;
import javax.inject.Inject;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // ── Login ──────────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<User>> login(String phoneOrEmail, String password) {
        return authRepository.login(phoneOrEmail, password);
    }

    // ── Register ───────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<User>> register(RegisterRequest request) {
        return authRepository.register(request);
    }

    // ── Check session ──────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<User>> checkSession() {
        return authRepository.checkSession();
    }

    // ── Logout ─────────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> logout() {
        return authRepository.logout();
    }

    // ── Forgot Password ────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<String>> forgotPassword(String phoneOrEmail) {
        return authRepository.forgotPassword(phoneOrEmail);
    }

    // ── Verify OTP ─────────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<String>> verifyOtp(String phoneOrEmail, String otp) {
        return authRepository.verifyOtp(phoneOrEmail, otp);
    }

    // ── Reset Password ─────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> resetPassword(String resetToken, String newPassword) {
        return authRepository.resetPassword(resetToken, newPassword);
    }

    // ── State ──────────────────────────────────────────────────────────────
    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser.setValue(user);
    }

    // ── Update Profile ─────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<User>> updateProfile(String name, String phone, String address) {
        return authRepository.updateProfile(name, phone, address);
    }

    // ── Change Password ────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Void>> changePassword(String oldPassword, String newPassword) {
        return authRepository.changePassword(oldPassword, newPassword);
    }
}
