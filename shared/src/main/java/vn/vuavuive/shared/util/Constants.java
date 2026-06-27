package vn.vuavuive.shared.util;

/**
 * Constants — Hằng số dùng chung toàn app.
 */
public final class Constants {

    private Constants() {} // Prevent instantiation

    // ── Order Statuses ──
    public static final String ORDER_STATUS_PENDING = "pending";
    public static final String ORDER_STATUS_PENDING_PAYMENT = "pending_payment";
    public static final String ORDER_STATUS_PENDING_APPROVAL = "pending_approval";
    public static final String ORDER_STATUS_CONFIRMED = "confirmed";
    public static final String ORDER_STATUS_PROCESSING = "processing";
    public static final String ORDER_STATUS_PACKED = "packed";
    public static final String ORDER_STATUS_SHIPPED = "shipped";
    public static final String ORDER_STATUS_DELIVERED = "delivered";
    public static final String ORDER_STATUS_CANCELLED = "cancelled";
    public static final String ORDER_STATUS_RETURN_REQUESTED = "return_requested";
    public static final String ORDER_STATUS_RETURN_APPROVED = "return_approved";
    public static final String ORDER_STATUS_RETURN_REJECTED = "return_rejected";

    // ── Payment Methods ──
    public static final String PAYMENT_COD = "cod";
    public static final String PAYMENT_VNPAY = "vnpay";
    public static final String PAYMENT_MOMO = "momo";

    // ── Payment Status ──
    public static final String PAYMENT_STATUS_PENDING = "pending";
    public static final String PAYMENT_STATUS_PAID = "paid";
    public static final String PAYMENT_STATUS_FAILED = "failed";
    public static final String PAYMENT_STATUS_CANCELLED = "cancelled";
    public static final String PAYMENT_STATUS_REFUNDED = "refunded";

    // ── User Roles ──
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_STAFF = "staff";
    public static final String ROLE_AUDIT = "audit";

    // ── Voucher Types ──
    public static final String VOUCHER_TYPE_SHIP = "ship";
    public static final String VOUCHER_TYPE_PERCENT = "percent";
    public static final String VOUCHER_TYPE_FIXED = "fixed";

    // ── Product Categories ──
    public static final String[] PRODUCT_CATEGORIES = {
        "Rau củ", "Trái cây", "Thịt tươi", "Hải sản",
        "Trứng & Sữa", "Gia vị", "Đồ khô", "Thực phẩm chế biến",
        "Đồ uống", "Khác"
    };

    // ── Shipment Carriers ──
    public static final String[] CARRIERS = {
        "GHN", "GHTK", "VNPost", "Viettel Post", "J&T", "Lalamove"
    };

    // ── Shipment Status ──
    public static final String SHIP_STATUS_PENDING = "pending";
    public static final String SHIP_STATUS_CONFIRMED = "confirmed";
    public static final String SHIP_STATUS_PICKED_UP = "picked_up";
    public static final String SHIP_STATUS_IN_TRANSIT = "in_transit";
    public static final String SHIP_STATUS_OUT_FOR_DELIVERY = "out_for_delivery";
    public static final String SHIP_STATUS_DELIVERED = "delivered";
    public static final String SHIP_STATUS_FAILED = "failed";
    public static final String SHIP_STATUS_RETURNING = "returning";
    public static final String SHIP_STATUS_RETURNED = "returned";

    // ── Pagination Defaults ──
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 20;

    // ── Regex ──
    public static final String REGEX_VN_PHONE = "^(0[3-9][0-9]{8}|\\+84[3-9][0-9]{8})$";
    public static final String REGEX_EMAIL = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    // ── Date Formats ──
    public static final String DATE_FORMAT_DISPLAY = "dd/MM/yyyy";
    public static final String DATE_FORMAT_DATETIME = "dd/MM/yyyy HH:mm";
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    // ── User Events for Recommendations ──
    public static final String EVENT_VIEW_PRODUCT = "view_product";
    public static final String EVENT_ADD_TO_CART = "add_to_cart";
    public static final String EVENT_PURCHASE = "purchase";
    public static final String EVENT_VIEW_RECIPE = "view_recipe";

    // ── SSE ──
    public static final String SSE_PATH = "/api/realtime/stream";
    public static final int SSE_RECONNECT_DELAY_MS = 5000;

    // ── Room Database ──
    public static final String DATABASE_NAME = "vvv_db";
    public static final int DATABASE_VERSION = 1;
}
