package lv.studymanager.repository;

import lv.studymanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByDueDateAscPriorityDesc(Long userId);
    List<Task> findByUserIdAndStatus(Long userId, Task.Status status);
    List<Task> findByUserIdAndCourseId(Long userId, Long courseId);
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.dueDate = :date AND t.status != 'DONE'")
    List<Task> findDueTodayByUserId(Long userId, LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueByUserId(Long userId, LocalDate today);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.status = 'DONE'")
    int countDoneByUserId(Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId")
    int countAllByUserId(Long userId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 'DONE'")
    long countDoneGlobal();
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :today AND t.status != 'DONE'")
    long countOverdueGlobal(LocalDate today);
}
