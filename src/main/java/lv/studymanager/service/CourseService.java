package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.Course;
import lv.studymanager.model.Task;
import lv.studymanager.model.User;
import lv.studymanager.repository.CourseRepository;
import lv.studymanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public List<CourseResponse> getAllCourses(String email) {
        Long userId = getUserId(email);
        return courseRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public CourseResponse createCourse(String email, CourseRequest req) {
        User user = findUser(email);
        Course course = Course.builder()
                .user(user)
                .name(req.getName())
                .instructor(req.getInstructor())
                .credits(req.getCredits())
                .color(req.getColor() != null ? req.getColor() : "#2E75B6")
                .description(req.getDescription())
                .status(parseStatus(req.getStatus(), Course.Status.ACTIVE))
                .category(parseCategory(req.getCategory(), Course.Category.MANDATORY))
                .build();
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(String email, Long id, CourseRequest req) {
        Course course = findCourse(id, getUserId(email));
        course.setName(req.getName());
        course.setInstructor(req.getInstructor());
        course.setCredits(req.getCredits());
        if (req.getColor() != null) course.setColor(req.getColor());
        course.setDescription(req.getDescription());
        course.setStatus(parseStatus(req.getStatus(), course.getStatus()));
        course.setCategory(parseCategory(req.getCategory(), course.getCategory()));
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(String email, Long id) {
        Course course = findCourse(id, getUserId(email));
        courseRepository.delete(course);
    }

    public CourseResponse getCourse(String email, Long id) {
        return toResponse(findCourse(id, getUserId(email)));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private CourseResponse toResponse(Course c) {
        CourseResponse r = new CourseResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setInstructor(c.getInstructor());
        r.setCredits(c.getCredits());
        r.setColor(c.getColor());
        r.setStatus(c.getStatus().name());
        r.setCategory(c.getCategory().name());
        r.setDescription(c.getDescription());
        r.setTotalTasks(c.getTasks().size());
        long done = c.getTasks().stream().filter(t -> t.getStatus() == Task.Status.DONE).count();
        r.setDoneTasks((int) done);
        r.setCompletionPercent(c.getCompletionPercent());
        return r;
    }

    private Course findCourse(Long id, Long userId) {
        return courseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Kurss nav atrasts: " + id));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Lietotājs nav atrasts"));
    }

    private Long getUserId(String email) {
        return findUser(email).getId();
    }

    private Course.Status parseStatus(String s, Course.Status fallback) {
        try { return s != null ? Course.Status.valueOf(s) : fallback; }
        catch (IllegalArgumentException e) { return fallback; }
    }

    private Course.Category parseCategory(String s, Course.Category fallback) {
        try { return s != null ? Course.Category.valueOf(s) : fallback; }
        catch (IllegalArgumentException e) { return fallback; }
    }
}
