package vn.vuavuive.backend.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.auth.dto.*;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;
import vn.vuavuive.backend.security.JwtUtils;
import java.time.LocalDateTime;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import java.util.Optional;
import java.util.Random;

/**
 * AuthService — Xử lý logic đăng ký, đăng nhập và làm mới token.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final OtpRepository otpRepository;
    private final TelegramNotificationService telegramNotificationService;
    private final ResendEmailService resendEmailService;

    /**
     * Đăng ký tài khoản mới.
     * Kiểm tra email và phone chưa tồn tại, mã hóa mật khẩu bằng BCrypt.
     */
    /**
     * Gửi mã OTP phục vụ đăng ký.
     * Chặn spam cooldown 60s, kiểm tra trùng số điện thoại, hash mật khẩu.
     */
    @Transactional
    public void sendRegisterOtp(RegisterOtpRequest request) {
        // Kiểm tra số điện thoại đã tồn tại trong bảng Users chính thức
        if (userRepository.existsByPhone(request.phone())) {
            throw AppException.conflict("Số điện thoại '" + request.phone() + "' đã được đăng ký");
        }
        // Kiểm tra email nếu được điền
        if (request.email() != null && !request.email().trim().isEmpty() && userRepository.existsByEmail(request.email())) {
            throw AppException.conflict("Email '" + request.email() + "' đã được đăng ký");
        }

        // Kiểm tra cooldown resend 60s
        Optional<Otp> existingOtpOpt = otpRepository.findTopByPhoneAndTypeOrderByCreatedAtDesc(request.phone(), "REGISTER");
        if (existingOtpOpt.isPresent()) {
            Otp existingOtp = existingOtpOpt.get();
            if (existingOtp.getLastSentAt() != null && parseDateTime(existingOtp.getLastSentAt()).plusSeconds(60).isAfter(LocalDateTime.now())) {
                throw new AppException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng đợi 60 giây trước khi yêu cầu gửi lại mã OTP");
            }
        }

        // Tạo mã OTP 6 số
        String rawOtp = String.format(java.util.Locale.getDefault(), "%06d", new Random().nextInt(1000000));
        String codeHash = passwordEncoder.encode(rawOtp);

        // Upsert Pending Registration
        PendingRegistration pending = pendingRegistrationRepository.findByPhone(request.phone())
                .orElse(new PendingRegistration());
        pending.setPhone(request.phone());
        pending.setFullName(request.fullName());
        pending.setEmail(request.email() != null && !request.email().trim().isEmpty() ? request.email().trim() : null);
        
        pending.setPasswordHash(passwordEncoder.encode(request.password()));
        
        pending.setAddress(request.address());
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(15).toString()); // Pending user exists for 15 minutes
        pendingRegistrationRepository.save(pending);

        // Lưu / Cập nhật OTP record
        Otp otp = existingOtpOpt.orElse(new Otp());
        otp.setPhone(request.phone());
        otp.setCodeHash(codeHash);
        otp.setType("REGISTER");
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5).toString()); // OTP valid for 5 mins
        otp.setIsUsed(false);
        otp.setAttemptCount(0);
        otp.setLastSentAt(LocalDateTime.now().toString());
        otpRepository.save(otp);

        // Gửi qua Telegram
        telegramNotificationService.sendOtp(request.phone(), rawOtp);

        // Gửi qua Resend Email Service nếu email không trống
        if (request.email() != null && !request.email().trim().isEmpty()) {
            resendEmailService.sendOtp(request.email(), rawOtp);
        }
    }

    /**
     * Xác thực mã OTP và tiến hành tạo tài khoản người dùng chính thức.
     */
    @Transactional
    public AuthResponse verifyRegisterOtp(VerifyOtpRequest request) {
        Otp otp = otpRepository.findTopByPhoneAndTypeOrderByCreatedAtDesc(request.phone(), "REGISTER")
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Không tìm thấy yêu cầu xác thực OTP cho số điện thoại này"));

        // Kiểm tra xem OTP đã được sử dụng hay hết hạn
        if (otp.getIsUsed()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã OTP đã được sử dụng. Vui lòng yêu cầu mã mới.");
        }
        if (otp.getExpiresAt() != null && parseDateTime(otp.getExpiresAt()).isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }
        if (otp.getAttemptCount() >= 5) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã OTP đã bị khóa do nhập sai quá 5 lần. Vui lòng yêu cầu mã mới.");
        }

        // So sánh khớp mã OTP
        if (!passwordEncoder.matches(request.code(), otp.getCodeHash())) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            if (otp.getAttemptCount() >= 5) {
                otp.setIsUsed(true); // Lock OTP
                otpRepository.save(otp);
                throw new AppException(HttpStatus.BAD_REQUEST, "Mã OTP đã bị khóa do nhập sai quá 5 lần. Vui lòng yêu cầu mã mới.");
            }
            otpRepository.save(otp);
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã OTP không chính xác. Bạn còn " + (5 - otp.getAttemptCount()) + " lần thử.");
        }

        // Tải Pending Registration
        PendingRegistration pending = pendingRegistrationRepository.findByPhone(request.phone())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Yêu cầu đăng ký đã hết hạn hoặc không tồn tại. Vui lòng thử lại."));

        if (pending.getExpiresAt() != null && parseDateTime(pending.getExpiresAt()).isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Yêu cầu đăng ký đã hết hạn. Vui lòng bắt đầu lại.");
        }

        // Tạo User chính thức
        User user = User.builder()
                .fullName(pending.getFullName())
                .email(pending.getEmail())
                .phone(pending.getPhone())
                .passwordHash(pending.getPasswordHash())
                .role(User.Role.CUSTOMER)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        // Vô hiệu hóa OTP và dọn dẹp Pending Registration
        otp.setIsUsed(true);
        otp.setUsedAt(LocalDateTime.now().toString());
        otpRepository.save(otp);
        pendingRegistrationRepository.delete(pending);

        return buildAuthResponse(user);
    }

    /**
     * Đăng ký tài khoản mới (Endpoint cũ trực tiếp - giữ lại fallback nếu cần)
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw AppException.conflict("Email '" + request.email() + "' đã được đăng ký");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw AppException.conflict("Số điện thoại '" + request.phone() + "' đã được đăng ký");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(User.Role.CUSTOMER)
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    /**
     * Đăng nhập bằng email / số điện thoại và mật khẩu.
     * Sử dụng tên đăng nhập (email hoặc sđt) phù hợp để Spring Security xác thực.
     */
    public AuthResponse login(LoginRequest request) {
        String identifier = request.identifier();
        
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không đúng"));

        try {
            String principal = user.getEmail() != null && !user.getEmail().isEmpty()
                    ? user.getEmail()
                    : user.getPhone();

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(principal, request.password())
            );
        } catch (BadCredentialsException e) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không đúng");
        }

        return buildAuthResponse(user);
    }

    /**
     * Làm mới Access Token từ Refresh Token.
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtUtils.validateToken(refreshToken)) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "Refresh Token không hợp lệ hoặc đã hết hạn, vui lòng đăng nhập lại");
        }

        String identifier = jwtUtils.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> AppException.notFound("User"));

        String subject = user.getEmail() != null && !user.getEmail().isEmpty()
                ? user.getEmail()
                : user.getPhone();

        String newAccessToken = jwtUtils.generateAccessToken(subject);

        return new AuthResponse(
                UUID.fromString(user.getId()),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                newAccessToken,
                refreshToken
        );
    }

    /** Tạo AuthResponse kèm cả Access Token và Refresh Token */
    private AuthResponse buildAuthResponse(User user) {
        String subject = user.getEmail() != null && !user.getEmail().isEmpty()
                ? user.getEmail()
                : user.getPhone();

        String accessToken  = jwtUtils.generateAccessToken(subject);
        String refreshToken = jwtUtils.generateRefreshToken(subject);

        return new AuthResponse(
                UUID.fromString(user.getId()),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                refreshToken
        );
    }

    public UserResponse getUserResponse(String identifier) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateProfile(String emailOrPhone, String name, String phone, String address) {
        User user = userRepository.findByEmail(emailOrPhone)
                .or(() -> userRepository.findByPhone(emailOrPhone))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        if (name != null) user.setFullName(name);
        if (phone != null) user.setPhone(phone);
        if (address != null) user.setAddress(address);

        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void changePassword(String emailOrPhone, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(emailOrPhone)
                .or(() -> userRepository.findByPhone(emailOrPhone))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return LocalDateTime.MIN;
        }
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            try {
                return LocalDateTime.ofInstant(Instant.parse(dateTimeStr), java.time.ZoneId.systemDefault());
            } catch (Exception ex) {
                try {
                    return java.time.OffsetDateTime.parse(dateTimeStr).toLocalDateTime();
                } catch (Exception ex2) {
                    log.error("Failed to parse date string: {}", dateTimeStr, ex2);
                    return LocalDateTime.MIN;
                }
            }
        }
    }
}
