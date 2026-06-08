package vn.vuavuive.backend.modules.shipper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, UUID> {
    
    Optional<Shipper> findByPhone(String phone);

    List<Shipper> findByCurrentStatusAndIsActiveTrue(Shipper.Status status);

    boolean existsByPhone(String phone);
}
