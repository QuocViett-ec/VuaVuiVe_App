package vn.vuavuive.shared.util;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * PortalScopeInterceptor — Gắn header X-Portal-Scope cho mọi request.
 * Customer app: "customer", Admin app: "admin".
 * Backend dùng header này để route đến đúng session cookie.
 */
public class PortalScopeInterceptor implements Interceptor {

    private static final String HEADER_NAME = "X-Portal-Scope";

    private final String portalScope;

    public PortalScopeInterceptor(String portalScope) {
        this.portalScope = portalScope;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request modified = original.newBuilder()
                .header(HEADER_NAME, portalScope)
                .build();
        return chain.proceed(modified);
    }
}
