package vn.vuavuive.backend.modules.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegistration extends BaseEntity {

    @Column(name = "phone", unique = true, nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "address")
    private String address;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
