package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.*;
import lv.studymanager.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private AchievementService achievementService;
    @InjectMocks private TaskService taskService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.lv")
            .firstName("Test").lastName("User").build();
        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));
    }

    @Test
    void getAllTasks_returnsList() {
        Task task = Task.builder().id(1L).user(user).title("Uzdevums")
            .priority(Task.Priority.HIGH).status(Task.Status.TODO).build();

        when(taskRepository.findByUserIdOrderByDueDateAscPriorityDesc(1L))
            .thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getAllTasks("test@test.lv");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Uzdevums");
        assertThat(result.get(0).getPriority()).isEqualTo(Task.Priority.HIGH);
    }

    @Test
    void getAllTasks_returnsEmptyList() {
        when(taskRepository.findByUserIdOrderByDueDateAscPriorityDesc(1L))
            .thenReturn(List.of());

        assertThat(taskService.getAllTasks("test@test.lv")).isEmpty();
    }

    @Test
    void createTask_noCourse_success() {
        TaskRequest req = new TaskRequest();
        req.setTitle("Darbs");
        req.setPriority(Task.Priority.MEDIUM);
        req.setDueDate(LocalDate.now().plusDays(3));

        Task saved = Task.builder().id(99L).user(user).title("Darbs")
            .priority(Task.Priority.MEDIUM).status(Task.Status.TODO).build();

        when(taskRepository.save(any())).thenReturn(saved);

        TaskResponse res = taskService.createTask("test@test.lv", req);

        assertThat(res.getId()).isEqualTo(99L);
        assertThat(res.getTitle()).isEqualTo("Darbs");
        verify(achievementService).checkAndAwardAchievements(1L);
    }

    @Test
    void createTask_withCourse_success() {
        Course course = Course.builder().id(5L).user(user).name("Math").color("#fff").build();

        TaskRequest req = new TaskRequest();
        req.setTitle("Darbs ar kursu");
        req.setCourseId(5L);

        Task saved = Task.builder().id(10L).user(user).course(course).title("Darbs ar kursu")
            .status(Task.Status.TODO).build();

        when(courseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(course));
        when(taskRepository.save(any())).thenReturn(saved);

        TaskResponse res = taskService.createTask("test@test.lv", req);

        assertThat(res.getCourseId()).isEqualTo(5L);
        assertThat(res.getCourseName()).isEqualTo("Math");
    }

    @Test
    void createTask_wrongCourse_throws() {
        TaskRequest req = new TaskRequest();
        req.setTitle("Test");
        req.setCourseId(999L);

        when(courseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask("test@test.lv", req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Kurss nav atrasts");
    }

    @Test
    void updateStatus_toDone_triggersAchievements() {
        Task task = Task.builder().id(1L).user(user).title("T").status(Task.Status.TODO).build();

        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        taskService.updateStatus("test@test.lv", 1L, Task.Status.DONE);

        verify(achievementService).checkAndAwardAchievements(1L);
    }

    @Test
    void updateStatus_toInProgress_noAchievements() {
        Task task = Task.builder().id(1L).user(user).title("T").status(Task.Status.TODO).build();

        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        taskService.updateStatus("test@test.lv", 1L, Task.Status.IN_PROGRESS);

        verify(achievementService, never()).checkAndAwardAchievements(anyLong());
    }

    @Test
    void deleteTask_success() {
        Task task = Task.builder().id(1L).user(user).title("T").build();

        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(task));

        taskService.deleteTask("test@test.lv", 1L);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_notOwned_throws() {
        when(taskRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask("test@test.lv", 999L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getDueToday_returnsList() {
        Task task = Task.builder().id(1L).user(user).title("Today")
            .dueDate(LocalDate.now()).status(Task.Status.TODO).build();

        when(taskRepository.findDueTodayByUserId(eq(1L), any())).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getDueToday("test@test.lv");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Today");
    }

    @Test
    void getOverdue_returnsList() {
        Task task = Task.builder().id(1L).user(user).title("Overdue")
            .dueDate(LocalDate.now().minusDays(1)).status(Task.Status.TODO).build();

        when(taskRepository.findOverdueByUserId(eq(1L), any())).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getOverdue("test@test.lv");

        assertThat(result).hasSize(1);
    }
}
