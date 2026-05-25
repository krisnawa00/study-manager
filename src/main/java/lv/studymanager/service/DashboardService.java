package lv.studymanager.service;

import lv.studymanager.dto.Dto.DashboardStats;
import lv.studymanager.repository.AchievementRepository;
import lv.studymanager.repository.CourseRepository;
import lv.studymanager.repository.TaskRepository;
import lv.studymanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;
    private final AchievementRepository achievementRepository;

    public DashboardStats getStats(String email) {
        Long userId = userRepository.findByEmail(email).orElseThrow().getId();

        int total      = taskRepository.countAllByUserId(userId);
        int done       = taskRepository.countDoneByUserId(userId);
        int overdue    = taskRepository.findOverdueByUserId(userId, LocalDate.now()).size();
        int dueToday   = taskRepository.findDueTodayByUserId(userId, LocalDate.now()).size();
        int courses    = (int) courseRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
        int active     = courseRepository.countActiveCoursesByUserId(userId);
        int badges     = achievementRepository.countByUserId(userId);
        double progress = total > 0 ? (double) done / total * 100 : 0.0;

        DashboardStats stats = new DashboardStats();
        stats.setTotalCourses(courses);
        stats.setActiveCourses(active);
        stats.setTotalTasks(total);
        stats.setDoneTasks(done);
        stats.setOverdueTasks(overdue);
        stats.setDueTodayTasks(dueToday);
        stats.setAchievements(badges);
        stats.setOverallProgress(Math.round(progress * 10.0) / 10.0);
        return stats;
    }
}
