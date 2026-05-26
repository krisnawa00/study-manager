package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.User;
import lv.studymanager.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L).email("test@test.lv")
            .firstName("Jānis").lastName("Bērziņš")
            .passwordHash("hashed").role(User.Role.STUDENT).build();
    }

    @Test
    void getProfile_success() {
        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));

        UserResponse res = userService.getProfile("test@test.lv");

        assertThat(res.getEmail()).isEqualTo("test@test.lv");
        assertThat(res.getFirstName()).isEqualTo("Jānis");
        assertThat(res.getLastName()).isEqualTo("Bērziņš");
    }

    @Test
    void getProfile_userNotFound_throws() {
        when(userRepository.findByEmail("missing@test.lv")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("missing@test.lv"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateProfile_success() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("Pēteris");
        req.setLastName("Kalniņš");

        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserResponse res = userService.updateProfile("test@test.lv", req);

        assertThat(res.getFirstName()).isEqualTo("Pēteris");
        assertThat(res.getLastName()).isEqualTo("Kalniņš");
    }

    @Test
    void changePassword_success() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("oldPass");
        req.setNewPassword("newPass123");

        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("newHashed");
        when(userRepository.save(any())).thenReturn(user);

        userService.changePassword("test@test.lv", req);

        verify(userRepository).save(any());
    }

    @Test
    void changePassword_wrongCurrentPassword_throws() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("wrong");
        req.setNewPassword("newPass123");

        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("test@test.lv", req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("nepareiza");
    }
}
