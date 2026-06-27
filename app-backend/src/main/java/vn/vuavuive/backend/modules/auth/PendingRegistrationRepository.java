package vn.vuavuive.backend.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PendingRegistrationRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<PendingRegistration> findById(String id) {
        return Optional.ofNullable(firebase.get("pendingRegistrations/" + id, PendingRegistration.class));
    }

    public List<PendingRegistration> findAll() {
        return firebase.getList("pendingRegistrations", PendingRegistration.class);
    }

    public PendingRegistration save(PendingRegistration registration) {
        if (registration.getId() == null) {
            registration.setId(UUID.randomUUID().toString());
        }
        firebase.save("pendingRegistrations/" + registration.getId(), registration);
        return registration;
    }

    public Optional<PendingRegistration> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        return findAll().stream()
                .filter(r -> phone.equals(r.getPhone()))
                .findFirst();
    }

    public void deleteByPhone(String phone) {
        if (phone == null) return;
        findByPhone(phone).ifPresent(r -> firebase.delete("pendingRegistrations/" + r.getId()));
    }

    public void delete(PendingRegistration registration) {
        if (registration != null && registration.getId() != null) {
            firebase.delete("pendingRegistrations/" + registration.getId());
        }
    }
}
