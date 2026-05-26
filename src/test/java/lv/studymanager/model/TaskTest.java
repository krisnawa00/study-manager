package lv.studymanager.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class TaskTest {

    @Test
    void isOverdue_pastDueNotDone_returnsTrue() {
        Task task = Task.builder()
            .dueDate(LocalDate.now().minusDays(1))
            .status(Task.Status.TODO)
            .build();
        assertThat(task.isOverdue()).isTrue();
    }

    @Test
    void isOverdue_futureDue_returnsFalse() {
        Task task = Task.builder()
            .dueDate(LocalDate.now().plusDays(1))
            .status(Task.Status.TODO)
            .build();
        assertThat(task.isOverdue()).isFalse();
    }

    @Test
    void isOverdue_pastDueDone_returnsFalse() {
        Task task = Task.builder()
            .dueDate(LocalDate.now().minusDays(1))
            .status(Task.Status.DONE)
            .build();
        assertThat(task.isOverdue()).isFalse();
    }

    @Test
    void isOverdue_noDueDate_returnsFalse() {
        Task task = Task.builder()
            .status(Task.Status.TODO)
            .build();
        assertThat(task.isOverdue()).isFalse();
    }

    @Test
    void defaultPriority_isMedium() {
        Task task = Task.builder().title("Test").build();
        assertThat(task.getPriority()).isEqualTo(Task.Priority.MEDIUM);
    }

    @Test
    void defaultStatus_isTodo() {
        Task task = Task.builder().title("Test").build();
        assertThat(task.getStatus()).isEqualTo(Task.Status.TODO);
    }
}
