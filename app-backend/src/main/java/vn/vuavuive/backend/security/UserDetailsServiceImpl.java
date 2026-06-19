package vn.vuavuive.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Implements UserDetailsService để Spring Security có thể load thông tin
 * người dùng từ Database khi xác thực JWT.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + identifier));

        if (!user.getIsActive()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản đã bị vô hiệu hóa");
        }

        String username = user.getEmail() != null && !user.getEmail().isEmpty()
                ? user.getEmail()
                : user.getPhone();

        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
