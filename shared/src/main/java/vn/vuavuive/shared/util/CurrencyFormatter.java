package vn.vuavuive.shared.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * CurrencyFormatter — Format tiền VNĐ.
 */
public class CurrencyFormatter {

    private static final NumberFormat VND_FORMAT =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public static String format(double amount) {
        return VND_FORMAT.format((long) amount) + " \u20ab";
    }

    public static String format(long amount) {
        return VND_FORMAT.format(amount) + " \u20ab";
    }

    // Alias used throughout app-admin
    public static String formatVnd(double amount) { return format(amount); }
    public static String formatVnd(Double amount) { return amount != null ? format(amount) : ""; }
    public static String formatVnd(long amount)   { return format(amount); }

    public static String formatCompact(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format(Locale.US, "%.1fB \u20ab", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            return String.format(Locale.US, "%.1fM \u20ab", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format(Locale.US, "%.0fK \u20ab", amount / 1_000);
        }
        return format(amount);
    }
}
