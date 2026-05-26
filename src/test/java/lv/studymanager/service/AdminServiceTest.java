package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.User;
import lv.studymanager.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private AchievementRepository achievementRepository;
    @InjectMocks private AdminService adminService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L).email("user@test.lv")
            .firstName("Jānis").lastName("Bērziņš")
            .role(User.Role.STUDENT).build();
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(taskRepository.countAllByUserId(1L)).thenReturn(5);

        List<AdminUserResponse> result = adminService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("user@test.lv");
        assertThat(result.get(0).getTotalTasks()).isEqualTo(5);
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.countAllByUserId(1L)).thenReturn(3);

        AdminUserResponse res = adminService.getUserById(1L);

        assertThat(res.getEmail()).isEqualTo("user@test.lv");
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getUserById(99L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("nav atrasts");
    }

    @Test
    void changeRole_toAdmin_success() {
        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(taskRepository.countAllByUserId(1L)).thenReturn(0);

        AdminUserResponse res = adminService.changeRole(1L, req);

        assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void changeRole_invalidRole_throws() {
        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRole("SUPERUSER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.changeRole(1L, req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nezināma loma");
    }

    @Test
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.deleteUser(1L, "admin@test.lv");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_selfDelete_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.deleteUser(1L, "user@test.lv"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nevar dzēst savu kontu");
    }

    @Test
    void getPlatformStats_returnsStats() {
        when(userRepository.count()).thenReturn(10L);
        when(courseRepository.count()).thenReturn(20L);
        when(taskRepository.count()).thenReturn(50L);
        when(taskRepository.countDoneGlobal()).thenReturn(30L);
        when(taskRepository.countOverdueGlobal(any())).thenReturn(5L);

        AdminStatsResponse stats = adminService.getPlatformStats();

        assertThat(stats.getTotalUsers()).isEqualTo(10L);
        assertThat(stats.getTotalCourses()).isEqualTo(20L);
        assertThat(stats.getTotalTasks()).isEqualTo(50L);
        assertThat(stats.getDoneTasks()).isEqualTo(30L);
        assertThat(stats.getOverdueTasks()).isEqualTo(5L);
        assertThat(stats.getPlatformCompletionRate()).isEqualTo(60.0);
    }
}
