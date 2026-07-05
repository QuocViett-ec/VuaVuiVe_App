package vn.vuavuive.backend.modules.order;

import static vn.vuavuive.backend.modules.order.Order.OrderStatus;

public final class OrderStateMachine {

    private OrderStateMachine() {}

    public static boolean canCustomerCancel(OrderStatus status) {
        return status == OrderStatus.PENDING_PAYMENT
                || status == OrderStatus.PENDING_APPROVAL;
    }

    public static boolean canAdminTransition(OrderStatus from, OrderStatus to) {
        if (to == OrderStatus.CANCELLED) {
            return from == OrderStatus.PENDING_PAYMENT
                    || from == OrderStatus.PENDING_APPROVAL;
        }
        return to == OrderStatus.CONFIRMED
                && from == OrderStatus.PENDING_APPROVAL;
    }

    public static boolean canAssignShipper(OrderStatus status) {
        return status == OrderStatus.CONFIRMED;
    }

    public static boolean canShipperTransition(
            OrderStatus from, OrderStatus to, boolean approvedCustomerReturn) {
        return (to == OrderStatus.IN_TRANSIT
                    && from == OrderStatus.CONFIRMED)
                || ((to == OrderStatus.DELIVERED || to == OrderStatus.FAILED)
                    && from == OrderStatus.IN_TRANSIT)
                || (to == OrderStatus.RETURNED
                    && (from == OrderStatus.FAILED
                        || (from == OrderStatus.DELIVERED && approvedCustomerReturn)));
    }
}
