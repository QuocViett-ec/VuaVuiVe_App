package vn.vuavuive.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    @Test
    void acceptsFirebaseUserWithoutPasswordHash() {
        UserRepository users = mock(UserRepository.class);
        User shipper = User.builder()
                .email("shipper@example.com")
                .role(User.Role.SHIPPER)
                .isActive(true)
                .build();
        when(users.findByEmail(shipper.getEmail())).thenReturn(Optional.of(shipper));

        UserDetails details = new UserDetailsServiceImpl(users).loadUserByUsername(shipper.getEmail());

        assertEquals("ROLE_SHIPPER", details.getAuthorities().iterator().next().getAuthority());
    }
}
