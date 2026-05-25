package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.Course;
import lv.studymanager.model.Task;
import lv.studymanager.model.User;
import lv.studymanager.repository.CourseRepository;
import lv.studymanager.repository.TaskRepository;
import lv.studymanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final AchievementService achievementService;

    public List<TaskResponse> getAllTasks(String email) {
        return taskRepository.findByUserIdOrderByDueDateAscPriorityDesc(getUserId(email))
                .stream().map(this::toResponse).toList();
    }

    public List<TaskResponse> getTasksByCourse(String email, Long courseId) {
        return taskRepository.findByUserIdAndCourseId(getUserId(email), courseId)
                .stream().map(this::toResponse).toList();
    }

    public List<TaskResponse> getDueToday(String email) {
        return taskRepository.findDueTodayByUserId(getUserId(email), LocalDate.now())
                .stream().map(this::toResponse).toList();
    }

    public List<TaskResponse> getOverdue(String email) {
        return taskRepository.findOverdueByUserId(getUserId(email), LocalDate.now())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskResponse createTask(String email, TaskRequest req) {
        User user = findUser(email);
        Course course = null;
        if (req.getCourseId() != null) {
            course = courseRepository.findByIdAndUserId(req.getCourseId(), user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Kurss nav atrasts"));
        }
        Task task = Task.builder()
                .user(user)
                .course(course)
                .title(req.getTitle())
                .description(req.getDescription())
                .priority(req.getPriority() != null ? req.getPriority() : Task.Priority.MEDIUM)
                .dueDate(req.getDueDate())
                .recurring(req.isRecurring())
                .recurrencePattern(req.getRecurrencePattern())
                .build();
        Task saved = taskRepository.save(task);
        achievementService.checkAndAwardAchievements(user.getId());
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(String email, Long id, TaskRequest req) {
        Task task = findTask(id, getUserId(email));
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        if (req.getPriority() != null) task.setPriority(req.getPriority());
        task.setDueDate(req.getDueDate());
        task.setRecurring(req.isRecurring());
        task.setRecurrencePattern(req.getRecurrencePattern());
        if (req.getCourseId() != null) {
            Course course = courseRepository.findByIdAndUserId(req.getCourseId(), getUserId(email))
                    .orElseThrow(() -> new IllegalArgumentException("Kurss nav atrasts"));
            task.setCourse(course);
        }
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(String email, Long id, Task.Status newStatus) {
        Task task = findTask(id, getUserId(email));
        task.setStatus(newStatus);
        Task saved = taskRepository.save(task);
        if (newStatus == Task.Status.DONE) {
            achievementService.checkAndAwardAchievements(getUserId(email));
        }
        return toResponse(saved);
    }

    @Transactional
    public void deleteTask(String email, Long id) {
        taskRepository.delete(findTask(id, getUserId(email)));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private TaskResponse toResponse(Task t) {
        TaskResponse r = new TaskResponse();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setDescription(t.getDescription());
        r.setPriority(t.getPriority());
        r.setStatus(t.getStatus());
        r.setDueDate(t.getDueDate());
        r.setOverdue(t.isOverdue());
        r.setCreatedAt(t.getCreatedAt());
        if (t.getCourse() != null) {
            r.setCourseId(t.getCourse().getId());
            r.setCourseName(t.getCourse().getName());
            r.setCourseColor(t.getCourse().getColor());
        }
        return r;
    }

    private Task findTask(Long id, Long userId) {
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Uzdevums nav atrasts: " + id));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Lietotājs nav atrasts"));
    }

    private Long getUserId(String email) {
        return findUser(email).getId();
    }
}
