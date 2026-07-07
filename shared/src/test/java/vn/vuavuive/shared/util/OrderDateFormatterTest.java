package vn.vuavuive.shared.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OrderDateFormatterTest {

    @Test
    public void formatsBackendLocalDateTime() {
        assertEquals("05/07/2026 08:12",
                OrderDateFormatter.format("2026-07-05T08:12:42.692766700"));
    }
}
