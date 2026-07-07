package vn.vuavuive.shared.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class OrderDateFormatter {
    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private OrderDateFormatter() {}

    public static String format(String raw) {
        if (raw == null || raw.isBlank()) return "-";
        String value = raw.trim();
        try {
            return DISPLAY.format(Instant.parse(value).atZone(ZoneId.systemDefault()));
        } catch (Exception ignored) {
        }
        try {
            return DISPLAY.format(LocalDateTime.parse(value.replace(" ", "T")));
        } catch (Exception ignored) {
        }
        return value.replace("T", " ");
    }
}
