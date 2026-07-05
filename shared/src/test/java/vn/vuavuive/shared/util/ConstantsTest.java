package vn.vuavuive.shared.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConstantsTest {

    @Test
    public void everyOrderStatusBelongsToOneUiGroup() {
        String[] statuses = {
                "PENDING_PAYMENT", "PENDING_APPROVAL",
                "CONFIRMED", "IN_TRANSIT",
                "DELIVERED", "FAILED", "CANCELLED",
                "RETURNED"
        };

        for (String status : statuses) {
            int groups = (Constants.isOrderPending(status) ? 1 : 0)
                    + (Constants.isOrderConfirmed(status) ? 1 : 0)
                    + (Constants.isOrderShipping(status) ? 1 : 0)
                    + (Constants.isOrderDelivered(status) ? 1 : 0)
                    + (Constants.isOrderCancelled(status) ? 1 : 0)
                    + (Constants.isOrderReturn(status) ? 1 : 0);
            assertEquals(status, 1, groups);
        }
    }
}
