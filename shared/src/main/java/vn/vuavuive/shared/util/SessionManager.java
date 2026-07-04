package vn.vuavuive.shared.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import com.google.gson.Gson;
import org.json.JSONObject;
import vn.vuavuive.shared.data.dto.User;

/**
 * SessionManager — Quản lý trạng thái đăng nhập và thông tin user.
 * Lưu thông tin user vào SharedPreferences sau khi login thành công.
 */
public class SessionManager {

    private static final String PREFS_NAME = "vvv_session";
    private static final String KEY_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final SharedPreferences prefs;
    private final Gson gson;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveUser(User user) {
        if (user == null) return;
        prefs.edit()
                .putString(KEY_USER, gson.toJson(user))
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public boolean saveSession(User user, String accessToken, String refreshToken) {
        if (user == null || accessToken == null || accessToken.isEmpty()) return false;
        prefs.edit()
                .putString(KEY_USER, gson.toJson(user))
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken != null ? refreshToken : "")
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
        return true;
    }

    public void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        if (accessToken != null && !accessToken.isEmpty()) {
            editor.putString(KEY_ACCESS_TOKEN, accessToken);
        } else {
            editor.remove(KEY_ACCESS_TOKEN);
        }
        if (refreshToken != null && !refreshToken.isEmpty()) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        } else {
            editor.remove(KEY_REFRESH_TOKEN);
        }
        editor.apply();
    }

    public User getUser() {
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson == null) return null;
        try {
            return gson.fromJson(userJson, User.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && hasValidAccessToken();
    }

    public void clearSession() {
        prefs.edit()
                .clear()
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public boolean hasValidAccessToken() {
        String token = getAccessToken();
        if (token == null || token.isEmpty()) return false;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;
            JSONObject payload = new JSONObject(new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP)));
            return payload.optLong("exp", 0) * 1000 > System.currentTimeMillis();
        } catch (Exception ignored) {
            return false;
        }
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }

    public String getUserRole() {
        User user = getUser();
        return user != null ? user.getRole() : null;
    }

    public boolean isAdmin() {
        String role = getUserRole();
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isBackoffice() {
        String role = getUserRole();
        return "admin".equalsIgnoreCase(role) || "staff".equalsIgnoreCase(role) || "audit".equalsIgnoreCase(role);
    }

    public boolean isShipper() {
        String role = getUserRole();
        return "shipper".equalsIgnoreCase(role);
    }
}
