package vn.vuavuive.backend.modules.shipper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ShipperRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<Shipper> findById(String id) {
        return Optional.ofNullable(firebase.get("shippers/" + id, Shipper.class));
    }

    public Optional<Shipper> findById(UUID id) {
        return findById(id.toString());
    }

    public List<Shipper> findAll() {
        return firebase.getList("shippers", Shipper.class);
    }

    public Shipper save(Shipper shipper) {
        if (shipper.getId() == null) {
            shipper.setId(UUID.randomUUID().toString());
        }
        firebase.save("shippers/" + shipper.getId(), shipper);
        return shipper;
    }

    public void deleteById(String id) {
        firebase.delete("shippers/" + id);
    }

    public Optional<Shipper> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        return findAll().stream()
                .filter(s -> phone.equals(s.getPhone()))
                .findFirst();
    }

    public Optional<Shipper> findByUserId(String userId) {
        if (userId == null) return Optional.empty();
        return findAll().stream()
                .filter(s -> userId.equals(s.getUserId()))
                .findFirst();
    }

    public Optional<Shipper> findByUserId(UUID userId) {
        if (userId == null) return Optional.empty();
        return findByUserId(userId.toString());
    }

    public List<Shipper> findByCurrentStatusAndIsActiveTrue(Shipper.Status status) {
        return findAll().stream()
                .filter(s -> status == s.getCurrentStatus() && Boolean.TRUE.equals(s.getIsActive()))
                .collect(Collectors.toList());
    }

    public boolean existsByPhone(String phone) {
        return findByPhone(phone).isPresent();
    }
}
