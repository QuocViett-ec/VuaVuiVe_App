package vn.vuavuive.backend.modules.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<User> findById(String id) {
        return Optional.ofNullable(firebase.get("users/" + id, User.class));
    }

    public Optional<User> findById(UUID id) {
        return findById(id.toString());
    }

    public List<User> findAll() {
        return firebase.getList("users", User.class);
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        firebase.save("users/" + user.getId(), user);
        return user;
    }

    public void deleteById(String id) {
        firebase.delete("users/" + id);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return findAll().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .sorted((u1, u2) -> {
                    boolean p1 = u1.getPasswordHash() != null && !u1.getPasswordHash().isEmpty();
                    boolean p2 = u2.getPasswordHash() != null && !u2.getPasswordHash().isEmpty();
                    if (p1 && !p2) return -1;
                    if (!p1 && p2) return 1;
                    return 0;
                })
                .findFirst();
    }

    public Optional<User> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        return findAll().stream()
                .filter(u -> phone.equals(u.getPhone()))
                .sorted((u1, u2) -> {
                    boolean p1 = u1.getPasswordHash() != null && !u1.getPasswordHash().isEmpty();
                    boolean p2 = u2.getPasswordHash() != null && !u2.getPasswordHash().isEmpty();
                    if (p1 && !p2) return -1;
                    if (!p1 && p2) return 1;
                    return 0;
                })
                .findFirst();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public boolean existsByPhone(String phone) {
        return findByPhone(phone).isPresent();
    }
}
