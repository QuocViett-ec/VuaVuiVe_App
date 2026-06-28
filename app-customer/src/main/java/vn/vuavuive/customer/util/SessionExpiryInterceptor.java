package vn.vuavuive.customer.util;

import android.content.Context;
import android.content.Intent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import vn.vuavuive.customer.ui.auth.LoginActivity;
import vn.vuavuive.shared.util.SessionManager;

@Singleton
public class SessionExpiryInterceptor implements Interceptor {

    private final Context appContext;
    private final SessionManager sessionManager;

    @Inject
    public SessionExpiryInterceptor(@ApplicationContext Context appContext, SessionManager sessionManager) {
        this.appContext = appContext;
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        if ((response.code() == 401 || response.code() == 403)
                && sessionManager.getAccessToken() != null
                && shouldForceLogin(request)) {
            sessionManager.clearSession();
            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.putExtra("session_expired", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
        }

        return response;
    }

    private boolean shouldForceLogin(Request request) {
        String path = request.url().encodedPath();
        return !path.startsWith("/api/auth/login")
                && !path.startsWith("/api/auth/register")
                && !path.startsWith("/api/auth/forgot-password")
                && !path.startsWith("/api/auth/verify")
                && !path.startsWith("/api/auth/reset-password");
    }
}
