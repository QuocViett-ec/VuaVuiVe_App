package vn.vuavuive.backend.modules.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vn.vuavuive.backend.modules.order.Order.OrderStatus.*;

class OrderStateMachineTest {

    @Test
    void acceptsOnlyTheApprovedFulfilmentFlow() {
        assertTrue(OrderStateMachine.canAdminTransition(PENDING_APPROVAL, CONFIRMED));
        assertTrue(OrderStateMachine.canAssignShipper(CONFIRMED));
        assertTrue(OrderStateMachine.canShipperTransition(CONFIRMED, IN_TRANSIT, false));
        assertTrue(OrderStateMachine.canShipperTransition(IN_TRANSIT, DELIVERED, false));
        assertTrue(OrderStateMachine.canShipperTransition(IN_TRANSIT, FAILED, false));
        assertTrue(OrderStateMachine.canShipperTransition(FAILED, RETURNED, false));
        assertTrue(OrderStateMachine.canShipperTransition(DELIVERED, RETURNED, true));

        assertFalse(OrderStateMachine.canAdminTransition(PENDING_APPROVAL, DELIVERED));
        assertFalse(OrderStateMachine.canAdminTransition(CONFIRMED, IN_TRANSIT));
        assertFalse(OrderStateMachine.canAdminTransition(CONFIRMED, CANCELLED));
        assertFalse(OrderStateMachine.canAssignShipper(PENDING_APPROVAL));
        assertFalse(OrderStateMachine.canCustomerCancel(CONFIRMED));
        assertFalse(OrderStateMachine.canShipperTransition(DELIVERED, RETURNED, false));
        assertFalse(OrderStateMachine.canCustomerCancel(IN_TRANSIT));
    }
}
