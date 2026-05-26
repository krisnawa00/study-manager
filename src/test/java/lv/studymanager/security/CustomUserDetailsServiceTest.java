package lv.studymanager.security;

import lv.studymanager.model.User;
import lv.studymanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_found_returnsUserDetails() {
        User user = User.builder()
            .id(1L).email("test@test.lv")
            .passwordHash("hashed")
            .role(User.Role.STUDENT).build();

        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("test@test.lv");

        assertThat(details.getUsername()).isEqualTo("test@test.lv");
        assertThat(details.getPassword()).isEqualTo("hashed");
        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByEmail("missing@test.lv")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@test.lv"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_adminRole_hasAdminAuthority() {
        User user = User.builder()
            .id(1L).email("admin@test.lv")
            .passwordHash("hashed")
            .role(User.Role.ADMIN).build();

        when(userRepository.findByEmail("admin@test.lv")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("admin@test.lv");

        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
