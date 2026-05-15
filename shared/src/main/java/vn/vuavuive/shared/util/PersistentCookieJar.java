package vn.vuavuive.shared.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * PersistentCookieJar — Lưu session cookie vào SharedPreferences.
 * Đảm bảo session được duy trì giữa các lần mở app.
 * Key "vvv.customer.sid" / "vvv.admin.sid" được backend sử dụng.
 */
public class PersistentCookieJar implements CookieJar {

    private static final String PREFS_NAME = "vvv_cookies";
    private static final String KEY_COOKIES = "cookies";

    private final SharedPreferences prefs;

    public PersistentCookieJar(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        Set<String> cookieStrings = new HashSet<>(
                prefs.getStringSet(KEY_COOKIES, new HashSet<>())
        );
        for (Cookie cookie : cookies) {
            // Encode cookie as string
            cookieStrings.removeIf(s -> getCookieName(s).equals(cookie.name()));
            cookieStrings.add(encodeCookie(cookie));
        }
        prefs.edit().putStringSet(KEY_COOKIES, cookieStrings).apply();
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        Set<String> cookieStrings = prefs.getStringSet(KEY_COOKIES, new HashSet<>());
        List<Cookie> cookies = new ArrayList<>();
        for (String cookieStr : cookieStrings) {
            Cookie cookie = decodeCookie(cookieStr, url);
            if (cookie != null) {
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    public void clearCookies() {
        prefs.edit().remove(KEY_COOKIES).apply();
    }

    private String encodeCookie(Cookie cookie) {
        return cookie.name() + "=" + cookie.value()
                + ";domain=" + cookie.domain()
                + ";path=" + cookie.path()
                + (cookie.secure() ? ";secure" : "")
                + (cookie.httpOnly() ? ";httponly" : "");
    }

    private String getCookieName(String encoded) {
        int eqIdx = encoded.indexOf('=');
        return eqIdx > 0 ? encoded.substring(0, eqIdx) : "";
    }

    private Cookie decodeCookie(String encoded, HttpUrl url) {
        try {
            String[] parts = encoded.split(";");
            if (parts.length == 0) return null;
            String[] nameValue = parts[0].split("=", 2);
            if (nameValue.length < 2) return null;

            Cookie.Builder builder = new Cookie.Builder()
                    .name(nameValue[0].trim())
                    .value(nameValue[1].trim())
                    .domain(url.host())
                    .path("/");

            for (int i = 1; i < parts.length; i++) {
                String part = parts[i].trim().toLowerCase();
                if (part.startsWith("domain=")) {
                    builder.domain(part.substring(7));
                } else if (part.startsWith("path=")) {
                    builder.path(part.substring(5));
                } else if (part.equals("secure")) {
                    builder.secure();
                } else if (part.equals("httponly")) {
                    builder.httpOnly();
                }
            }
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }
}
