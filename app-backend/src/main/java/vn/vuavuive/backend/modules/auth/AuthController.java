package vn.vuavuive.backend.modules.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.auth.dto.*;

/**
 * AuthController — Tất cả endpoint liên quan đến xác thực người dùng.
 *
 * Các API được expose (PUBLIC — Không cần token):
 * POST /api/auth/register   — Đăng ký tài khoản
 * POST /api/auth/login      — Đăng nhập
 * POST /api/auth/refresh    — Làm mới Access Token
 * GET  /api/auth/me         - Lấy thông tin user hiện tại
 * POST /api/auth/logout     - Đăng xuất
 */
@Tag(name = "Authentication", description = "API xác thực người dùng (Đăng ký, Đăng nhập)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng ký tài khoản khách hàng mới")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        AuthResponse auth = authService.register(request);
        UserResponse user = authService.getUserResponse(request.email());

        // Set Cookie
        setAuthCookie(response, auth.accessToken(), user.role());

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Đăng ký tài khoản thành công")
                .data(user)
                .accessToken(auth.accessToken())
                .refreshToken(auth.refreshToken())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @Operation(summary = "Đăng nhập bằng email và mật khẩu (Customer)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse auth = authService.login(request);
        UserResponse user = authService.getUserResponse(request.identifier());

        // Set Cookie
        setAuthCookie(response, auth.accessToken(), user.role());

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Đăng nhập thành công")
                .data(user)
                .accessToken(auth.accessToken())
                .refreshToken(auth.refreshToken())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Đăng nhập bằng email và mật khẩu cho Admin/Staff/Audit")
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<UserResponse>> adminLogin(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse auth = authService.login(request);
        UserResponse user = authService.getUserResponse(request.identifier());

        if (!"admin".equals(user.role()) && !"staff".equals(user.role()) && !"audit".equals(user.role())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền đăng nhập vào trang quản trị");
        }

        // Set Cookie
        setAuthCookie(response, auth.accessToken(), user.role());

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Đăng nhập thành công")
                .data(user)
                .accessToken(auth.accessToken())
                .refreshToken(auth.refreshToken())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Lấy thông tin người dùng đang đăng nhập")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe() {
        org.springframework.security.core.Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }

        String email = authentication.getName();
        UserResponse user = authService.getUserResponse(email);

        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Lấy thông tin người dùng thành công")
                .data(user)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Đăng xuất tài khoản")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        clearCookie(response, "vvv.customer.sid");
        clearCookie(response, "vvv.admin.sid");

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .message("Đăng xuất thành công")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(
        summary = "Làm mới Access Token",
        description = "App Android gọi khi Access Token hết hạn (15 phút). " +
                      "Gửi Refresh Token để nhận Access Token mới mà không cần đăng nhập lại."
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    private void setAuthCookie(HttpServletResponse response, String token, String role) {
        String cookieName = "vvv.customer.sid";
        if ("admin".equals(role) || "staff".equals(role) || "audit".equals(role)) {
            cookieName = "vvv.admin.sid";
        }
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(cookieName, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String cookieName) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
