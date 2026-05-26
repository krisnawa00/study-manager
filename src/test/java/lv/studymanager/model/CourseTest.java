package lv.studymanager.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class CourseTest {

    @Test
    void getCompletionPercent_noTasks_returnsZero() {
        Course course = Course.builder().name("Math").build();
        assertThat(course.getCompletionPercent()).isEqualTo(0.0);
    }

    @Test
    void getCompletionPercent_allDone_returns100() {
        Task t1 = Task.builder().status(Task.Status.DONE).title("T1").build();
        Task t2 = Task.builder().status(Task.Status.DONE).title("T2").build();
        Course course = Course.builder().name("Math").tasks(List.of(t1, t2)).build();
        assertThat(course.getCompletionPercent()).isEqualTo(100.0);
    }

    @Test
    void getCompletionPercent_halfDone_returns50() {
        Task t1 = Task.builder().status(Task.Status.DONE).title("T1").build();
        Task t2 = Task.builder().status(Task.Status.TODO).title("T2").build();
        Course course = Course.builder().name("Math").tasks(List.of(t1, t2)).build();
        assertThat(course.getCompletionPercent()).isEqualTo(50.0);
    }

    @Test
    void defaultStatus_isActive() {
        Course course = Course.builder().name("Math").build();
        assertThat(course.getStatus()).isEqualTo(Course.Status.ACTIVE);
    }

    @Test
    void defaultCategory_isMandatory() {
        Course course = Course.builder().name("Math").build();
        assertThat(course.getCategory()).isEqualTo(Course.Category.MANDATORY);
    }

    @Test
    void defaultColor_isBlue() {
        Course course = Course.builder().name("Math").build();
        assertThat(course.getColor()).isEqualTo("#2E75B6");
    }
}
