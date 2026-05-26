package lv.studymanager.repository;

import lv.studymanager.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private TaskRepository taskRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AchievementRepository achievementRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = em.persist(User.builder()
            .firstName("Test").lastName("User")
            .email("repo@test.lv").passwordHash("hash")
            .build());
        em.flush();
    }

    // ── UserRepository ───────────────────────────────────────────────

    @Test
    void findByEmail_found() {
        assertThat(userRepository.findByEmail("repo@test.lv")).isPresent();
    }

    @Test
    void findByEmail_notFound() {
        assertThat(userRepository.findByEmail("nobody@test.lv")).isEmpty();
    }

    @Test
    void existsByEmail_true() {
        assertThat(userRepository.existsByEmail("repo@test.lv")).isTrue();
    }

    @Test
    void existsByEmail_false() {
        assertThat(userRepository.existsByEmail("nobody@test.lv")).isFalse();
    }

    // ── TaskRepository ───────────────────────────────────────────────

    @Test
    void findDueTodayByUserId_returnsOnlyTodayNonDone() {
        em.persist(Task.builder().user(user).title("Today")
            .dueDate(LocalDate.now()).status(Task.Status.TODO).build());
        em.persist(Task.builder().user(user).title("Done today")
            .dueDate(LocalDate.now()).status(Task.Status.DONE).build());
        em.persist(Task.builder().user(user).title("Tomorrow")
            .dueDate(LocalDate.now().plusDays(1)).status(Task.Status.TODO).build());
        em.flush();

        List<Task> result = taskRepository.findDueTodayByUserId(user.getId(), LocalDate.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Today");
    }

    @Test
    void findOverdueByUserId_returnsOnlyOverdueNonDone() {
        em.persist(Task.builder().user(user).title("Overdue")
            .dueDate(LocalDate.now().minusDays(1)).status(Task.Status.TODO).build());
        em.persist(Task.builder().user(user).title("Overdue done")
            .dueDate(LocalDate.now().minusDays(1)).status(Task.Status.DONE).build());
        em.flush();

        List<Task> result = taskRepository.findOverdueByUserId(user.getId(), LocalDate.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Overdue");
    }

    @Test
    void countDoneByUserId_countsCorrectly() {
        em.persist(Task.builder().user(user).title("T1").status(Task.Status.DONE).build());
        em.persist(Task.builder().user(user).title("T2").status(Task.Status.DONE).build());
        em.persist(Task.builder().user(user).title("T3").status(Task.Status.TODO).build());
        em.flush();

        assertThat(taskRepository.countDoneByUserId(user.getId())).isEqualTo(2);
    }

    @Test
    void countAllByUserId_countsCorrectly() {
        em.persist(Task.builder().user(user).title("T1").status(Task.Status.TODO).build());
        em.persist(Task.builder().user(user).title("T2").status(Task.Status.TODO).build());
        em.flush();

        assertThat(taskRepository.countAllByUserId(user.getId())).isEqualTo(2);
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        Task task = em.persist(Task.builder().user(user).title("T1").build());
        em.flush();

        assertThat(taskRepository.findByIdAndUserId(task.getId(), 999L)).isEmpty();
    }

    // ── CourseRepository ─────────────────────────────────────────────

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsCourses() {
        em.persist(Course.builder().user(user).name("Math").build());
        em.persist(Course.builder().user(user).name("Physics").build());
        em.flush();

        List<Course> result = courseRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void course_findByIdAndUserId_wrongUser_returnsEmpty() {
        Course course = em.persist(Course.builder().user(user).name("Math").build());
        em.flush();

        assertThat(courseRepository.findByIdAndUserId(course.getId(), 999L)).isEmpty();
    }

    @Test
    void countActiveCoursesByUserId_countsCorrectly() {
        em.persist(Course.builder().user(user).name("Active")
            .status(Course.Status.ACTIVE).build());
        em.persist(Course.builder().user(user).name("Completed")
            .status(Course.Status.COMPLETED).build());
        em.flush();

        assertThat(courseRepository.countActiveCoursesByUserId(user.getId())).isEqualTo(1);
    }

    // ── AchievementRepository ────────────────────────────────────────

    @Test
    void existsByUserIdAndBadgeType_true() {
        em.persist(Achievement.builder().user(user)
            .badgeType(Achievement.BadgeType.FIRST_TASK)
            .description("Test").build());
        em.flush();

        assertThat(achievementRepository.existsByUserIdAndBadgeType(
            user.getId(), Achievement.BadgeType.FIRST_TASK)).isTrue();
    }

    @Test
    void existsByUserIdAndBadgeType_false() {
        assertThat(achievementRepository.existsByUserIdAndBadgeType(
            user.getId(), Achievement.BadgeType.CENTURY)).isFalse();
    }

    @Test
    void countByUserId_countsCorrectly() {
        em.persist(Achievement.builder().user(user)
            .badgeType(Achievement.BadgeType.FIRST_TASK).description("T").build());
        em.persist(Achievement.builder().user(user)
            .badgeType(Achievement.BadgeType.CENTURY).description("T").build());
        em.flush();

        assertThat(achievementRepository.countByUserId(user.getId())).isEqualTo(2);
    }
}
