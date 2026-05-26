package lv.studymanager.service;

import lv.studymanager.dto.Dto.DashboardStats;
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
class DashboardServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private AchievementRepository achievementRepository;
    @InjectMocks private DashboardService dashboardService;

    @Test
    void getStats_returnsCorrectValues() {
        User user = User.builder().id(1L).email("test@test.lv").build();

        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));
        when(taskRepository.countAllByUserId(1L)).thenReturn(10);
        when(taskRepository.countDoneByUserId(1L)).thenReturn(5);
        when(taskRepository.findOverdueByUserId(eq(1L), any())).thenReturn(List.of());
        when(taskRepository.findDueTodayByUserId(eq(1L), any())).thenReturn(List.of());
        when(courseRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(courseRepository.countActiveCoursesByUserId(1L)).thenReturn(3);
        when(achievementRepository.countByUserId(1L)).thenReturn(2);

        DashboardStats stats = dashboardService.getStats("test@test.lv");

        assertThat(stats.getTotalTasks()).isEqualTo(10);
        assertThat(stats.getDoneTasks()).isEqualTo(5);
        assertThat(stats.getActiveCourses()).isEqualTo(3);
        assertThat(stats.getAchievements()).isEqualTo(2);
        assertThat(stats.getOverallProgress()).isEqualTo(50.0);
    }

    @Test
    void getStats_noTasks_progressIsZero() {
        User user = User.builder().id(1L).email("test@test.lv").build();

        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));
        when(taskRepository.countAllByUserId(1L)).thenReturn(0);
        when(taskRepository.countDoneByUserId(1L)).thenReturn(0);
        when(taskRepository.findOverdueByUserId(eq(1L), any())).thenReturn(List.of());
        when(taskRepository.findDueTodayByUserId(eq(1L), any())).thenReturn(List.of());
        when(courseRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(courseRepository.countActiveCoursesByUserId(1L)).thenReturn(0);
        when(achievementRepository.countByUserId(1L)).thenReturn(0);

        DashboardStats stats = dashboardService.getStats("test@test.lv");

        assertThat(stats.getOverallProgress()).isEqualTo(0.0);
    }
}
