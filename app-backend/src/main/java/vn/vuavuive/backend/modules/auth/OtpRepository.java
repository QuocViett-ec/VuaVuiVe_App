package vn.vuavuive.backend.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OtpRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<Otp> findById(String id) {
        return Optional.ofNullable(firebase.get("otps/" + id, Otp.class));
    }

    public List<Otp> findAll() {
        return firebase.getList("otps", Otp.class);
    }

    public Otp save(Otp otp) {
        if (otp.getId() == null) {
            otp.setId(UUID.randomUUID().toString());
        }
        firebase.save("otps/" + otp.getId(), otp);
        return otp;
    }

    public Optional<Otp> findTopByPhoneAndTypeOrderByCreatedAtDesc(String phone, String type) {
        if (phone == null || type == null) return Optional.empty();
        return findAll().stream()
                .filter(o -> phone.equals(o.getPhone()) && type.equals(o.getType()))
                .sorted(Comparator.comparing(Otp::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .findFirst();
    }
}
