package vn.vuavuive.shared.fcm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.vuavuive.shared.data.api.NotificationApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.util.AuthInterceptor;
import vn.vuavuive.shared.util.PortalScopeInterceptor;
import vn.vuavuive.shared.util.SessionManager;

public final class FcmTokenRegistrar {
    private static final String TAG = "FcmTokenRegistrar";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 9201;
    private static final String META_BASE_URL = "vn.vuavuive.BASE_URL";
    private static final String META_PORTAL_SCOPE = "vn.vuavuive.PORTAL_SCOPE";

    private FcmTokenRegistrar() {}

    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 33
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    public static void register(Context context, String... topics) {
        Context appContext = context.getApplicationContext();
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> registerToken(appContext, token));
        for (String topic : topics) {
            if (topic != null && !topic.isBlank()) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic);
            }
        }
    }

    public static void registerToken(Context context, String token) {
        if (token == null || token.isBlank()) return;
        SessionManager sessionManager = new SessionManager(context);
        if (!sessionManager.isLoggedIn()) return;

        String baseUrl = meta(context, META_BASE_URL);
        String scope = meta(context, META_PORTAL_SCOPE);
        if (baseUrl == null || baseUrl.isBlank()) return;
        if (!baseUrl.endsWith("/")) baseUrl += "/";

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessionManager))
                .addInterceptor(new PortalScopeInterceptor(scope != null ? scope : "app"))
                .build();
        NotificationApi api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NotificationApi.class);

        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("platform", "android");
        body.put("appScope", scope != null ? scope : "app");
        api.registerDeviceToken(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "FCM token register failed: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.w(TAG, "FCM token register failed", t);
            }
        });
    }

    private static String meta(Context context, String key) {
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = info.metaData;
            return metaData != null ? metaData.getString(key) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
