package vn.vuavuive.shared.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import vn.vuavuive.shared.data.dto.User;

/**
 * SessionManager — Quản lý trạng thái đăng nhập và thông tin user.
 * Lưu thông tin user vào SharedPreferences sau khi login thành công.
 */
public class SessionManager {

    private static final String PREFS_NAME = "vvv_session";
    private static final String KEY_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final Gson gson;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveUser(User user) {
        prefs.edit()
                .putString(KEY_USER, gson.toJson(user))
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public User getUser() {
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson == null) return null;
        return gson.fromJson(userJson, User.class);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_USER)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
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
        return "admin".equals(role);
    }

    public boolean isBackoffice() {
        String role = getUserRole();
        return "admin".equals(role) || "staff".equals(role) || "audit".equals(role);
    }

    public boolean isShipper() {
        String role = getUserRole();
        return "SHIPPER".equalsIgnoreCase(role);
    }
}
