package vn.vuavuive.shared.util;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor CSRF — CRITICAL.
 * Backend yêu cầu header X-Requested-With: XMLHttpRequest
 * cho tất cả POST/PUT/PATCH/DELETE.
 * Thiếu header này sẽ bị 403 "CSRF validation failed".
 *
 * Các endpoint KHÔNG cần: login, register, google, forgot-password,
 * verify-otp, reset-password, momo/ipn.
 */
public class CsrfInterceptor implements Interceptor {

    private static final String HEADER_NAME = "X-Requested-With";
    private static final String HEADER_VALUE = "XMLHttpRequest";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String method = original.method().toUpperCase();

        // Chỉ gắn header cho state-changing methods
        if (method.equals("POST") || method.equals("PUT")
                || method.equals("PATCH") || method.equals("DELETE")) {

            // Kiểm tra có phải endpoint exempt không
            String url = original.url().encodedPath();
            if (!isExemptEndpoint(url)) {
                Request modified = original.newBuilder()
                        .header(HEADER_NAME, HEADER_VALUE)
                        .build();
                return chain.proceed(modified);
            }
        }

        return chain.proceed(original);
    }

    /**
     * Các endpoint không cần CSRF header.
     */
    private boolean isExemptEndpoint(String path) {
        return path.contains("/auth/login")
                || path.contains("/auth/admin/login")
                || path.contains("/auth/register")
                || path.contains("/auth/google")
                || path.contains("/auth/forgot-password")
                || path.contains("/auth/verify-otp")
                || path.contains("/auth/reset-password")
                || path.contains("/momo/ipn");
    }
}
