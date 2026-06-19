package vn.vuavuive.backend.modules.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findTopByPhoneAndTypeOrderByCreatedAtDesc(String phone, String type);
}
