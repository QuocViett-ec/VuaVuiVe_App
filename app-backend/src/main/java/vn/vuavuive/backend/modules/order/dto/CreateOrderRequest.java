package vn.vuavuive.backend.modules.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO tạo đơn hàng mới — Tương thích 100% với cấu trúc gửi từ App Android.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    /** Danh sách sản phẩm trong đơn */
    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    private List<OrderItemRequest> items;

    /** Thông tin người nhận và địa chỉ */
    @NotNull(message = "Thông tin giao hàng không được để trống")
    @Valid
    private DeliveryInfo delivery;

    /** Thông tin phương thức thanh toán */
    @NotEmpty(message = "Phương thức thanh toán không được để trống")
    private Map<String, String> payment;

    /** Ghi chú của khách */
    private String note;

    /** Mã voucher áp dụng (tùy chọn) */
    private String voucherCode;
    private Double shippingFee;
    private Double discount;

    /**
     * Getter tương thích với chữ ký record cũ cho items()
     */
    public List<OrderItemRequest> items() {
        return this.items;
    }

    /**
     * Getter tương thích với chữ ký record cũ cho note()
     */
    public String note() {
        return this.note;
    }

    public BigDecimal shippingFeeAmount() {
        return BigDecimal.valueOf(shippingFee == null ? 0 : shippingFee);
    }

    public BigDecimal discountAmount() {
        return BigDecimal.valueOf(discount == null ? 0 : discount);
    }

    /**
     * Lấy địa chỉ giao hàng hoàn chỉnh dưới dạng chuỗi (Tương thích chữ ký cũ của record)
     */
    public String deliveryAddress() {
        if (delivery == null) return "";
        StringBuilder sb = new StringBuilder();
        if (delivery.name() != null && !delivery.name().trim().isEmpty()) {
            sb.append(delivery.name().trim());
        }
        if (delivery.phone() != null && !delivery.phone().trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" (");
            }
            sb.append(delivery.phone().trim());
            if (sb.length() > 0) {
                sb.append(")");
            }
        }
        if (delivery.address() != null && !delivery.address().trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(": ");
            }
            sb.append(delivery.address().trim());
        }
        return sb.toString();
    }

    /**
     * Lấy phương thức thanh toán viết hoa (Tương thích chữ ký cũ của record)
     */
    public String paymentMethod() {
        if (payment == null) return "COD";
        String method = payment.get("method");
        if (method == null || method.trim().isEmpty()) {
            return "COD";
        }
        return method.trim().toUpperCase(); // "COD", "VNPAY", "MOMO"
    }

    /**
     * Record đại diện cho thông tin giao hàng của client
     */
    public record DeliveryInfo(
        @NotBlank(message = "Tên người nhận không được để trống")
        String name,
        @NotBlank(message = "Số điện thoại không được để trống")
        String phone,
        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        String address
    ) {}
}
