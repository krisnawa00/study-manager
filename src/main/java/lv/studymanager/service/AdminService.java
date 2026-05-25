package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.User;
import lv.studymanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository        userRepository;
    private final CourseRepository     courseRepository;
    private final TaskRepository       taskRepository;
    private final AchievementRepository achievementRepository;

    // ── User management ──────────────────────────────────────────────

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    public AdminUserResponse getUserById(Long id) {
        return toAdminUserResponse(findUser(id));
    }

    @Transactional
    public AdminUserResponse changeRole(Long userId, ChangeRoleRequest req) {
        User user = findUser(userId);
        User.Role newRole;
        try {
            newRole = User.Role.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Nezināma loma: " + req.getRole() + ". Pieejamās: STUDENT, ADMIN");
        }
        user.setRole(newRole);
        User saved = userRepository.save(user);
        log.info("Admin changed role of user {} to {}", userId, newRole);
        return toAdminUserResponse(saved);
    }

    @Transactional
    public void deleteUser(Long userId, String callerEmail) {
        User target = findUser(userId);
        if (target.getEmail().equalsIgnoreCase(callerEmail)) {
            throw new IllegalArgumentException("Nevar dzēst savu kontu");
        }
        userRepository.delete(target);
        log.info("Admin deleted user {} ({})", userId, target.getEmail());
    }

    // ── Platform statistics ───────────────────────────────────────────

    public AdminStatsResponse getPlatformStats() {
        long users   = userRepository.count();
        long courses = courseRepository.count();
        long tasks   = taskRepository.count();
        long done    = taskRepository.countDoneGlobal();
        long overdue = taskRepository.countOverdueGlobal(LocalDate.now());

        double rate = tasks > 0
            ? Math.round((double) done / tasks * 1000.0) / 10.0
            : 0.0;

        AdminStatsResponse stats = new AdminStatsResponse();
        stats.setTotalUsers(users);
        stats.setTotalCourses(courses);
        stats.setTotalTasks(tasks);
        stats.setDoneTasks(done);
        stats.setOverdueTasks(overdue);
        stats.setPlatformCompletionRate(rate);
        return stats;
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private User findUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Lietotājs nav atrasts: " + id));
    }

    private AdminUserResponse toAdminUserResponse(User u) {
        AdminUserResponse r = new AdminUserResponse();
        r.setId(u.getId());
        r.setFirstName(u.getFirstName());
        r.setLastName(u.getLastName());
        r.setEmail(u.getEmail());
        r.setAvatarUrl(u.getAvatarUrl());
        r.setRole(u.getRole().name());
        r.setTotalCourses(u.getCourses().size());
        r.setTotalTasks(taskRepository.countAllByUserId(u.getId()));
        r.setCreatedAt(u.getCreatedAt());
        r.setUpdatedAt(u.getUpdatedAt());
        return r;
    }
}