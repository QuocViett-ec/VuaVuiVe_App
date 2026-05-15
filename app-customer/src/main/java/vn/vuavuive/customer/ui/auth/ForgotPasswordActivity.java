package vn.vuavuive.customer.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.AuthViewModel;

@AndroidEntryPoint
public class ForgotPasswordActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private AuthViewModel authViewModel;

    // Shared data between steps
    String enteredPhoneEmail = "";
    String resetToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ForgotPasswordPagerAdapter(this));
        viewPager.setUserInputEnabled(false); // Manual navigation only
    }

    void goToStep(int step) {
        viewPager.setCurrentItem(step, true);
    }

    // ── Step Adapter ───────────────────────────────────────────────────────
    private class ForgotPasswordPagerAdapter extends FragmentStateAdapter {
        ForgotPasswordPagerAdapter(AppCompatActivity activity) { super(activity); }

        @Override public int getItemCount() { return 3; }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new Step1Fragment();
                case 1: return new Step2Fragment();
                case 2: return new Step3Fragment();
                default: return new Step1Fragment();
            }
        }
    }

    // ── Step 1: Nhập email/phone ───────────────────────────────────────────
    public static class Step1Fragment extends Fragment {
        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate simple layout inline
            View view = inflater.inflate(R.layout.fragment_forgot_step1, container, false);
            ForgotPasswordActivity activity = (ForgotPasswordActivity) requireActivity();
            AuthViewModel vm = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

            EditText etInput = view.findViewById(R.id.et_phone_email);
            MaterialButton btnNext = view.findViewById(R.id.btn_next);
            ProgressBar progress = view.findViewById(R.id.progress_bar);
            TextView tvError = view.findViewById(R.id.tv_error);

            btnNext.setOnClickListener(v -> {
                String input = etInput.getText() != null ? etInput.getText().toString().trim() : "";
                if (TextUtils.isEmpty(input)) {
                    tvError.setText("Vui lòng nhập số điện thoại hoặc email");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                tvError.setVisibility(View.GONE);
                btnNext.setEnabled(false);
                progress.setVisibility(View.VISIBLE);

                vm.forgotPassword(input).observe(requireActivity(), result -> {
                    progress.setVisibility(View.GONE);
                    btnNext.setEnabled(true);
                    if (result.status == AuthRepository.Result.Status.SUCCESS) {
                        activity.enteredPhoneEmail = input;
                        activity.goToStep(1);
                    } else if (result.status == AuthRepository.Result.Status.ERROR) {
                        tvError.setText(result.message);
                        tvError.setVisibility(View.VISIBLE);
                    }
                });
            });

            return view;
        }
    }

    // ── Step 2: Nhập OTP ───────────────────────────────────────────────────
    public static class Step2Fragment extends Fragment {
        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_forgot_step2, container, false);
            ForgotPasswordActivity activity = (ForgotPasswordActivity) requireActivity();
            AuthViewModel vm = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

            EditText etOtp = view.findViewById(R.id.et_otp);
            MaterialButton btnVerify = view.findViewById(R.id.btn_verify);
            ProgressBar progress = view.findViewById(R.id.progress_bar);
            TextView tvError = view.findViewById(R.id.tv_error);

            btnVerify.setOnClickListener(v -> {
                String otp = etOtp.getText() != null ? etOtp.getText().toString().trim() : "";
                if (otp.length() != 6) {
                    tvError.setText("OTP phải có 6 chữ số");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                tvError.setVisibility(View.GONE);
                btnVerify.setEnabled(false);
                progress.setVisibility(View.VISIBLE);

                vm.verifyOtp(activity.enteredPhoneEmail, otp).observe(requireActivity(), result -> {
                    progress.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                    if (result.status == AuthRepository.Result.Status.SUCCESS) {
                        activity.resetToken = otp; // Use OTP as token indicator
                        activity.goToStep(2);
                    } else if (result.status == AuthRepository.Result.Status.ERROR) {
                        tvError.setText(result.message);
                        tvError.setVisibility(View.VISIBLE);
                    }
                });
            });

            return view;
        }
    }

    // ── Step 3: Đặt mật khẩu mới ──────────────────────────────────────────
    public static class Step3Fragment extends Fragment {
        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_forgot_step3, container, false);
            ForgotPasswordActivity activity = (ForgotPasswordActivity) requireActivity();
            AuthViewModel vm = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

            EditText etNewPass = view.findViewById(R.id.et_new_password);
            EditText etConfirm = view.findViewById(R.id.et_confirm_password);
            MaterialButton btnReset = view.findViewById(R.id.btn_reset);
            ProgressBar progress = view.findViewById(R.id.progress_bar);
            TextView tvError = view.findViewById(R.id.tv_error);

            btnReset.setOnClickListener(v -> {
                String newPass = etNewPass.getText() != null ? etNewPass.getText().toString() : "";
                String confirm = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

                if (newPass.length() < 6) {
                    tvError.setText("Mật khẩu phải có ít nhất 6 ký tự");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                if (!newPass.equals(confirm)) {
                    tvError.setText("Mật khẩu xác nhận không khớp");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                tvError.setVisibility(View.GONE);
                btnReset.setEnabled(false);
                progress.setVisibility(View.VISIBLE);

                vm.resetPassword(activity.resetToken, newPass).observe(requireActivity(), result -> {
                    progress.setVisibility(View.GONE);
                    btnReset.setEnabled(true);
                    if (result.status == AuthRepository.Result.Status.SUCCESS) {
                        // Go back to Login
                        activity.finish();
                    } else if (result.status == AuthRepository.Result.Status.ERROR) {
                        tvError.setText(result.message);
                        tvError.setVisibility(View.VISIBLE);
                    }
                });
            });

            return view;
        }
    }
}
