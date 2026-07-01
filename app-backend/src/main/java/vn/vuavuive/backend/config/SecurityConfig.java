package vn.vuavuive.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.vuavuive.backend.security.JwtAuthFilter;
import vn.vuavuive.backend.security.UserDetailsServiceImpl;

import java.util.List;

/**
 * Cấu hình Spring Security toàn bộ ứng dụng.
 *
 * Quy tắc phân quyền:
 * - PUBLIC: Đăng ký, Đăng nhập, Xem danh sách SP, Xem chi tiết SP
 * - AUTHENTICATED: Xem giỏ hàng, Đặt hàng, Xem đơn cá nhân
 * - ADMIN/STAFF: Quản lý SP, Order, User
 * - SHIPPER: Cập nhật trạng thái giao hàng
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Cho phép dùng @PreAuthorize trên method Service/Controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF (không cần thiết cho REST API với JWT)
            .csrf(AbstractHttpConfigurer::disable)

            // Cấu hình CORS (cho phép App Android và ngrok gọi API)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless session — Không dùng session, dùng JWT
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Cấu hình phân quyền các endpoint
            .authorizeHttpRequests(auth -> auth
                // ===== PUBLIC ENDPOINTS =====
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/recipes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                // Webhook thanh toán từ MoMo/ZaloPay phải public (họ gọi vào, không có token)
                .requestMatchers(HttpMethod.POST, "/api/payments/momo/ipn").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/payments/momo/return", "/api/payments/momo/mock", "/api/payments/momo/mock-result").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/payments/zalopay/callback").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/payments/zalopay/return", "/api/payments/zalopay/mock", "/api/payments/zalopay/mock-result").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/momo/ipn").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/momo/return", "/api/momo/mock", "/api/momo/mock-result").permitAll()

                // ===== SHIPPER ENDPOINTS =====
                .requestMatchers("/api/shippers/**").hasAnyRole("SHIPPER", "ADMIN")

                // ===== ADMIN / STAFF ENDPOINTS =====
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/uploads/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                // ===== TẤT CẢ CÒN LẠI — Cần đăng nhập =====
                .anyRequest().authenticated()
            )

            // Thêm JWT Filter TRƯỚC UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * Cấu hình CORS — Cho phép App Android (và Ngrok) gọi API từ bất kỳ nguồn nào.
     * Trong môi trường Production, cần giới hạn lại allowedOrigins.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));  // Dev: cho phép tất cả
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** BCrypt — Thuật toán mã hóa mật khẩu mạnh nhất hiện tại */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
