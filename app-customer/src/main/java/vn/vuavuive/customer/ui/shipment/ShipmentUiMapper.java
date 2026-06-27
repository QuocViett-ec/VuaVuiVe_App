package vn.vuavuive.customer.ui.shipment;

import vn.vuavuive.customer.R;

public final class ShipmentUiMapper {

    private ShipmentUiMapper() {}

    public static String getStatusLabel(String status) {
        if (status == null) return "--";
        switch (status) {
            case "pending":
                return "Cho xu ly";
            case "pending_payment":
                return "Cho thanh toan";
            case "pending_approval":
                return "Cho admin duyet";
            case "confirmed":
                return "Da xac nhan";
            case "picked":
            case "picked_up":
                return "Da lay hang";
            case "packed":
                return "Da dong goi";
            case "shipped":
                return "Da gui";
            case "in_transit":
                return "Dang van chuyen";
            case "out_for_delivery":
                return "Dang giao";
            case "delivered":
                return "Da giao";
            case "failed":
                return "Giao that bai";
            case "returning":
                return "Dang tra";
            case "returned":
                return "Da tra";
            case "cancelled":
                return "Da huy";
            default:
                return status;
        }
    }

    public static int getStatusColor(String status) {
        if (status == null) return R.color.text_secondary;
        switch (status) {
            case "pending":
                return R.color.status_pending;
            case "pending_payment":
            case "pending_approval":
                return R.color.status_pending;
            case "confirmed":
            case "picked":
            case "picked_up":
            case "packed":
                return R.color.status_confirmed;
            case "shipped":
            case "in_transit":
            case "out_for_delivery":
                return R.color.status_shipping;
            case "delivered":
                return R.color.status_delivered;
            case "failed":
                return R.color.error;
            case "returning":
            case "returned":
                return R.color.status_return;
            case "cancelled":
                return R.color.status_cancelled;
            default:
                return R.color.text_secondary;
        }
    }
}
