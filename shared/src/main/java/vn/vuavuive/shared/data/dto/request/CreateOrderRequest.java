package vn.vuavuive.shared.data.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import vn.vuavuive.shared.data.dto.DeliveryInfo;

public class CreateOrderRequest {

    @SerializedName("items")
    private List<OrderItemRequest> items;

    @SerializedName("delivery")
    private DeliveryInfo delivery;

    @SerializedName("payment")
    private Map<String, String> payment;

    @SerializedName("paymentMethod")
    private String paymentMethod;  // "cod", "vnpay", "momo"

    @SerializedName("voucherCode")
    private String voucherCode;

    @SerializedName("note")
    private String note;

    @SerializedName("subtotal")
    private double subtotal;

    @SerializedName("shippingFee")
    private double shippingFee;

    @SerializedName("discount")
    private double discount;

    @SerializedName("totalAmount")
    private double totalAmount;

    /** No-arg constructor for use in CheckoutActivity */
    public CreateOrderRequest() {}

    /** Full constructor */
    public CreateOrderRequest(List<OrderItemRequest> items, DeliveryInfo delivery,
                               String paymentMethod, String voucherCode, String note) {
        this.items = items;
        this.delivery = delivery;
        this.paymentMethod = paymentMethod;
        this.voucherCode = voucherCode;
        this.note = note;
    }

    // Getters & Setters
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public DeliveryInfo getDelivery() { return delivery; }
    public void setDelivery(DeliveryInfo delivery) { this.delivery = delivery; }

    public Map<String, String> getPayment() { return payment; }
    public void setPayment(Map<String, String> payment) { this.payment = payment; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    // ── Inner class ─────────────────────────────────────────────────────────
    public static class OrderItemRequest {
        @SerializedName("productId")
        private String productId;

        @SerializedName("productName")
        private String productName;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("price")
        private double price;

        @SerializedName("subtotal")
        private double subtotal;

        public OrderItemRequest() {}

        public OrderItemRequest(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public double getSubtotal() { return subtotal; }
        public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    }
}
