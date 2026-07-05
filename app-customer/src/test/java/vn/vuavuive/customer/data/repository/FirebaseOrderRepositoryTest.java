package vn.vuavuive.customer.data.repository;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FirebaseOrderRepositoryTest {

    @Test
    public void acceptsBackendAndLegacyFirebaseUserIds() {
        assertTrue(FirebaseOrderRepository.ownsOrder("backend-id", "backend-id", "firebase-id"));
        assertTrue(FirebaseOrderRepository.ownsOrder("firebase-id", "backend-id", "firebase-id"));
        assertFalse(FirebaseOrderRepository.ownsOrder("other-id", "backend-id", "firebase-id"));
        assertFalse(FirebaseOrderRepository.ownsOrder(null, "backend-id", "firebase-id"));
    }
}
