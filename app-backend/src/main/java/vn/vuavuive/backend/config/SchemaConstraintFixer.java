package vn.vuavuive.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchemaConstraintFixer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            try (java.sql.Connection conn = java.util.Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
                String dbProduct = conn.getMetaData().getDatabaseProductName();
                if (dbProduct != null && dbProduct.toLowerCase().contains("sqlite")) {
                    return; // Skip constraint fixing for SQLite
                }
            }
        } catch (Exception ignored) {}

        // ponytail: Hibernate update does not refresh old enum check constraints in Postgres.
        fix("orders", "orders_payment_status_check",
                "payment_status in ('UNPAID','PENDING','PAID','FAILED','CANCELLED','REFUNDED')");
        fix("orders", "orders_status_check",
                "status in ('PENDING','CONFIRMED','SHIPPING','PREPARING','READY_FOR_PICKUP','IN_TRANSIT','DELIVERED','FAILED','RETURNED','CANCELLED')");
        fix("order_status_logs", "order_status_logs_status_check",
                "status in ('PENDING','CONFIRMED','SHIPPING','PREPARING','READY_FOR_PICKUP','IN_TRANSIT','DELIVERED','FAILED','RETURNED','CANCELLED')");
        fix("payment_transactions", "payment_transactions_status_check",
                "status in ('PENDING','PAID','FAILED','CANCELLED')");
    }

    private void fix(String table, String constraint, String check) {
        try {
            jdbcTemplate.execute("alter table if exists " + table + " drop constraint if exists " + constraint);
            jdbcTemplate.execute("alter table if exists " + table + " add constraint " + constraint + " check (" + check + ")");
        } catch (Exception e) {
            // Ignore in SQLite, but we can hack it by modifying sqlite_master
            try {
                jdbcTemplate.execute("PRAGMA writable_schema = 1");
                jdbcTemplate.execute("UPDATE sqlite_master SET sql = REPLACE(sql, '''CONFIRMED'',''PREPARING''', '''CONFIRMED'',''SHIPPING'',''PREPARING''') WHERE type = 'table' AND name = '" + table + "'");
                jdbcTemplate.execute("PRAGMA writable_schema = 0");
            } catch (Exception ex) {
                // ignore
            }
        }
    }
}
