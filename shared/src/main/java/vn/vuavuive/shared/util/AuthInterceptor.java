package vn.vuavuive.shared.util;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * AuthInterceptor — Tự động gắn header "Authorization: Bearer <token>" vào mọi request.
 * Được inject vào OkHttpClient, hoạt động với tất cả các API cần xác thực.
 */
public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = sessionManager.getAccessToken();

        Request original = chain.request();

        // Chỉ đính kèm token nếu có, không ghi đè nếu request đã có Authorization header
        if (token != null && !token.isEmpty() && original.header("Authorization") == null) {
            Request modified = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(modified);
        }

        return chain.proceed(original);
    }
}
