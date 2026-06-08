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

/**
 * AuthService — Xử lý logic đăng ký, đăng nhập và làm mới token.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    /**
     * Đăng ký tài khoản mới.
     * Kiểm tra email và phone chưa tồn tại, mã hóa mật khẩu bằng BCrypt.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.email())) {
            throw AppException.conflict("Email '" + request.email() + "' đã được đăng ký");
        }
        // Kiểm tra số điện thoại đã tồn tại
        if (userRepository.existsByPhone(request.phone())) {
            throw AppException.conflict("Số điện thoại '" + request.phone() + "' đã được đăng ký");
        }

        // Tạo user mới với mật khẩu đã mã hóa
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
     * Đăng nhập bằng email + mật khẩu.
     * Dùng AuthenticationManager của Spring Security để xác thực — tự động so sánh BCrypt.
     */
    public AuthResponse login(LoginRequest request) {
        String identifier = request.identifier();
        
        // Tìm user bằng email hoặc số điện thoại
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không đúng"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không đúng");
        }

        return buildAuthResponse(user);
    }

    /**
     * Làm mới Access Token từ Refresh Token.
     * App Android gọi API này khi Access Token hết hạn (15 phút).
     * Trả về Access Token mới mà không cần đăng nhập lại.
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtUtils.validateToken(refreshToken)) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "Refresh Token không hợp lệ hoặc đã hết hạn, vui lòng đăng nhập lại");
        }

        String email = jwtUtils.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User"));

        // Chỉ cấp Access Token mới, giữ nguyên Refresh Token cũ
        String newAccessToken = jwtUtils.generateAccessToken(email);

        return new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                newAccessToken,
                refreshToken   // Giữ nguyên Refresh Token cũ
        );
    }

    /** Tạo AuthResponse kèm cả Access Token và Refresh Token */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtUtils.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                user.getId(),
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
}
