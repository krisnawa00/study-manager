package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.User;
import lv.studymanager.repository.UserRepository;
import lv.studymanager.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuthenticationManager authManager;
    @InjectMocks private AuthService authService;

    private RegisterRequest makeRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Jānis");
        req.setLastName("Bērziņš");
        req.setEmail("janis@test.lv");
        req.setPassword("password123");
        return req;
    }

    @Test
    void register_success_returnsTokens() {
        when(userRepository.existsByEmail("janis@test.lv")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(tokenProvider.generateAccessToken("janis@test.lv")).thenReturn("access");
        when(tokenProvider.generateRefreshToken("janis@test.lv")).thenReturn("refresh");

        AuthResponse res = authService.register(makeRegisterRequest());

        assertThat(res.getAccessToken()).isEqualTo("access");
        assertThat(res.getRefreshToken()).isEqualTo("refresh");
        assertThat(res.getUser().getEmail()).isEqualTo("janis@test.lv");
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail("janis@test.lv")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(makeRegisterRequest()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("jau reģistrēts");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsTokens() {
        LoginRequest req = new LoginRequest();
        req.setEmail("janis@test.lv");
        req.setPassword("password123");

        User user = User.builder()
            .id(1L).email("janis@test.lv")
            .firstName("Jānis").lastName("Bērziņš")
            .passwordHash("hashed").role(User.Role.STUDENT).build();

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken("janis@test.lv", "password123");

        when(authManager.authenticate(any())).thenReturn(authToken);
        when(userRepository.findByEmail("janis@test.lv")).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(authToken)).thenReturn("access");
        when(tokenProvider.generateRefreshToken("janis@test.lv")).thenReturn("refresh");

        AuthResponse res = authService.login(req);

        assertThat(res.getAccessToken()).isEqualTo("access");
        assertThat(res.getUser().getRole()).isEqualTo("STUDENT");
    }

    @Test
    void login_wrongPassword_throws() {
        LoginRequest req = new LoginRequest();
        req.setEmail("janis@test.lv");
        req.setPassword("wrong");

        when(authManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_invalidToken_throws() {
        when(tokenProvider.validateToken("bad")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("bad"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nederīgs");
    }

    @Test
    void refresh_validToken_returnsNewTokens() {
        User user = User.builder()
            .id(1L).email("janis@test.lv")
            .firstName("Jānis").lastName("Bērziņš")
            .role(User.Role.STUDENT).build();

        when(tokenProvider.validateToken("valid-refresh")).thenReturn(true);
        when(tokenProvider.getEmailFromToken("valid-refresh")).thenReturn("janis@test.lv");
        when(userRepository.findByEmail("janis@test.lv")).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken("janis@test.lv")).thenReturn("new-access");
        when(tokenProvider.generateRefreshToken("janis@test.lv")).thenReturn("new-refresh");

        AuthResponse res = authService.refresh("valid-refresh");

        assertThat(res.getAccessToken()).isEqualTo("new-access");
        assertThat(res.getRefreshToken()).isEqualTo("new-refresh");
    }
}
