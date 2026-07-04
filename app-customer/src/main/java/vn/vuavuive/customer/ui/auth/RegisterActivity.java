package vn.vuavuive.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.ui.MainActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.shared.data.dto.request.RegisterRequest;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.KeyEvent;
import android.widget.EditText;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    private TextInputLayout tilName, tilPhone, tilEmail, tilPassword, tilConfirmPassword, tilAddress;
    private TextInputEditText etName, etPhone, etEmail, etPassword, etConfirmPassword, etAddress;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvError, tvLoginLink;

    // Vietnamese phone pattern: 0[3-9]xxxxxxxx
    private static final java.util.regex.Pattern PHONE_PATTERN =
            java.util.regex.Pattern.compile("^0[3-9][0-9]{8}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilName            = findViewById(R.id.til_name);
        tilPhone           = findViewById(R.id.til_phone);
        tilEmail           = findViewById(R.id.til_email);
        tilPassword        = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilAddress         = findViewById(R.id.til_address);
        etName             = findViewById(R.id.et_name);
        etPhone            = findViewById(R.id.et_phone);
        etEmail            = findViewById(R.id.et_email);
        etPassword         = findViewById(R.id.et_password);
        etConfirmPassword  = findViewById(R.id.et_confirm_password);
        etAddress          = findViewById(R.id.et_address);
        btnRegister        = findViewById(R.id.btn_register);
        progressBar        = findViewById(R.id.progress_bar);
        tvError            = findViewById(R.id.tv_error);
        tvLoginLink        = findViewById(R.id.tv_login_link);

        // Back button
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private final androidx.activity.result.ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    String address = result.getData().getStringExtra(vn.vuavuive.customer.ui.checkout.MapPickerActivity.EXTRA_ADDRESS);
                    if (address != null && !address.isEmpty() && etAddress != null) {
                        etAddress.setText(address);
                    }
                }
            });

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLoginLink.setOnClickListener(v -> finish());
        
        if (tilAddress != null) {
            tilAddress.setEndIconOnClickListener(v -> {
                Intent intent = new Intent(this, vn.vuavuive.customer.ui.checkout.MapPickerActivity.class);
                mapPickerLauncher.launch(intent);
            });
        }
    }

    private void attemptRegister() {
        clearErrors();

        String name    = getText(etName);
        String phone   = getText(etPhone);
        String email   = getText(etEmail);
        String address = getText(etAddress);

        String pass    = getText(etPassword);
        String confirm = getText(etConfirmPassword);

        boolean valid = true;

        if (name.length() < 2) {
            tilName.setError("Họ tên phải có ít nhất 2 ký tự");
            valid = false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            tilPhone.setError("Số điện thoại không hợp lệ (VD: 0912345678)");
            valid = false;
        }
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            valid = false;
        }
        if (TextUtils.isEmpty(pass)) {
            tilPassword.setError("Mật khẩu không được để trống");
            valid = false;
        } else if (pass.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            valid = false;
        }
        if (TextUtils.isEmpty(confirm)) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            valid = false;
        } else if (!pass.equals(confirm)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            valid = false;
        }

        if (!valid) return;

        setLoading(true);

        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setPhone(phone);
        request.setEmail(email);
        request.setPassword(pass);
        request.setAddress(address.isEmpty() ? null : address);

        authViewModel.sendRegisterOtp(request).observe(this, result -> {
            switch (result.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    showOtpVerificationDialog(request);
                    break;
                case ERROR:
                    setLoading(false);
                    showError(result.message);
                    break;
            }
        });
    }

    private android.app.AlertDialog otpDialog;
    private android.os.CountDownTimer countDownTimer;

    private void showOtpVerificationDialog(RegisterRequest registerRequest) {
        if (isFinishing()) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_otp_verification, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        otpDialog = builder.create();
        if (otpDialog.getWindow() != null) {
            otpDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvSubtitle = dialogView.findViewById(R.id.tv_dialog_subtitle);
        EditText etOtp1 = dialogView.findViewById(R.id.et_otp1);
        EditText etOtp2 = dialogView.findViewById(R.id.et_otp2);
        EditText etOtp3 = dialogView.findViewById(R.id.et_otp3);
        EditText etOtp4 = dialogView.findViewById(R.id.et_otp4);
        EditText etOtp5 = dialogView.findViewById(R.id.et_otp5);
        EditText etOtp6 = dialogView.findViewById(R.id.et_otp6);
        EditText[] otpFields = new EditText[]{etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};

        setupOtpInputAutoMovement(otpFields);

        TextView tvOtpError = dialogView.findViewById(R.id.tv_otp_error);
        TextView tvCountdown = dialogView.findViewById(R.id.tv_countdown);
        TextView tvResendOtp = dialogView.findViewById(R.id.tv_resend_otp);
        MaterialButton btnVerifyOtp = dialogView.findViewById(R.id.btn_verify_otp);
        MaterialButton btnCancelOtp = dialogView.findViewById(R.id.btn_cancel_otp);

        // Subtitle text masking email & phone
        String emailStr = registerRequest.getEmail();
        String phoneStr = registerRequest.getPhone();
        String maskedEmail = "";
        if (emailStr != null && emailStr.contains("@")) {
            int atIndex = emailStr.indexOf("@");
            if (atIndex > 3) {
                maskedEmail = emailStr.substring(0, 3) + "***" + emailStr.substring(atIndex);
            } else {
                maskedEmail = "***" + emailStr.substring(atIndex);
            }
        }
        String maskedPhone = "";
        if (phoneStr != null && phoneStr.length() >= 7) {
            maskedPhone = phoneStr.substring(0, 3) + "****" + phoneStr.substring(phoneStr.length() - 3);
        } else {
            maskedPhone = phoneStr;
        }

        if (emailStr != null && !emailStr.trim().isEmpty()) {
            tvSubtitle.setText("Chúng tôi đã gửi một mã xác thực gồm 6 chữ số đến " + maskedPhone + " và email " + maskedEmail + ". Vui lòng nhập mã để tiếp tục.");
        } else {
            tvSubtitle.setText("Chúng tôi đã gửi một mã xác thực gồm 6 chữ số đến số điện thoại " + maskedPhone + ". Vui lòng nhập mã để tiếp tục.");
        }

        // Start 60s countdown timer
        startOtpCountdown(tvCountdown, tvResendOtp, registerRequest);

        btnVerifyOtp.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            for (EditText et : otpFields) {
                sb.append(et.getText().toString().trim());
            }
            String otpCode = sb.toString();
            if (otpCode.length() != 6) {
                tvOtpError.setText("Vui lòng nhập đầy đủ mã OTP 6 số");
                tvOtpError.setVisibility(View.VISIBLE);
                return;
            }

            tvOtpError.setVisibility(View.GONE);
            btnVerifyOtp.setEnabled(false);
            btnCancelOtp.setEnabled(false);

            authViewModel.verifyRegisterOtp(phoneStr, otpCode).observe(this, verifyResult -> {
                switch (verifyResult.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        if (otpDialog != null && otpDialog.isShowing()) {
                            otpDialog.dismiss();
                        }
                        goToMain();
                        break;
                    case ERROR:
                        btnVerifyOtp.setEnabled(true);
                        btnCancelOtp.setEnabled(true);
                        tvOtpError.setText(verifyResult.message);
                        tvOtpError.setVisibility(View.VISIBLE);
                        break;
                }
            });
        });

        tvResendOtp.setOnClickListener(v -> {
            tvOtpError.setVisibility(View.GONE);
            tvResendOtp.setVisibility(View.GONE);
            tvCountdown.setVisibility(View.VISIBLE);

            authViewModel.sendRegisterOtp(registerRequest).observe(this, resendResult -> {
                switch (resendResult.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        android.widget.Toast.makeText(RegisterActivity.this, "Mã OTP mới đã được gửi", android.widget.Toast.LENGTH_SHORT).show();
                        startOtpCountdown(tvCountdown, tvResendOtp, registerRequest);
                        break;
                    case ERROR:
                        tvResendOtp.setVisibility(View.VISIBLE);
                        tvCountdown.setVisibility(View.GONE);
                        tvOtpError.setText("Gửi lại OTP thất bại: " + resendResult.message);
                        tvOtpError.setVisibility(View.VISIBLE);
                        break;
                }
            });
        });

        btnCancelOtp.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            if (otpDialog != null && otpDialog.isShowing()) {
                otpDialog.dismiss();
            }
        });

        otpDialog.show();
    }

    private void startOtpCountdown(TextView tvCountdown, TextView tvResendOtp, RegisterRequest registerRequest) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        tvCountdown.setVisibility(View.VISIBLE);
        tvResendOtp.setVisibility(View.GONE);

        countDownTimer = new android.os.CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvCountdown.setText("Gửi lại mã(" + String.format(java.util.Locale.getDefault(), "00:%02d", seconds) + ")");
            }

            @Override
            public void onFinish() {
                tvCountdown.setVisibility(View.GONE);
                tvResendOtp.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void setupOtpInputAutoMovement(EditText[] otpFields) {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                private boolean isSelfChange = false;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isSelfChange) return;
                    String text = s.toString();
                    if (text.length() >= 6) {
                        isSelfChange = true;
                        String cleanOtp = text.replaceAll("\\D", "");
                        if (cleanOtp.length() > 6) cleanOtp = cleanOtp.substring(0, 6);
                        for (int j = 0; j < cleanOtp.length(); j++) {
                            otpFields[j].setText(String.valueOf(cleanOtp.charAt(j)));
                        }
                        if (cleanOtp.length() > 0) {
                            otpFields[Math.min(cleanOtp.length() - 1, 5)].requestFocus();
                        }
                        isSelfChange = false;
                        return;
                    }
                    if (text.length() == 1) {
                        if (index < 5) {
                            otpFields[index + 1].requestFocus();
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpFields[index].getText().length() == 0 && index > 0) {
                        otpFields[index - 1].setText("");
                        otpFields[index - 1].requestFocus();
                        return true;
                    }
                }
                return false;
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (otpDialog != null && otpDialog.isShowing()) {
            otpDialog.dismiss();
        }
        super.onDestroy();
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors() {
        tilName.setError(null);
        tilPhone.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tvError.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
