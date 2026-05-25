package lv.studymanager.service;

import lv.studymanager.model.Achievement;
import lv.studymanager.model.Achievement.BadgeType;
import lv.studymanager.model.User;
import lv.studymanager.repository.AchievementRepository;
import lv.studymanager.repository.TaskRepository;
import lv.studymanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<Achievement> getAchievements(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow().getId();
        return achievementRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }

    @Transactional
    public void checkAndAwardAchievements(Long userId) {
        int totalTasks = taskRepository.countAllByUserId(userId);
        int doneTasks  = taskRepository.countDoneByUserId(userId);

        award(userId, BadgeType.FIRST_TASK,  totalTasks >= 1);
        award(userId, BadgeType.CENTURY,     totalTasks >= 100);
        award(userId, BadgeType.EARLY_BIRD,  doneTasks  >= 1);  // refined in real logic
    }

    private void award(Long userId, BadgeType type, boolean condition) {
        if (condition && !achievementRepository.existsByUserIdAndBadgeType(userId, type)) {
            User user = userRepository.getReferenceById(userId);
            Achievement a = Achievement.builder()
                    .user(user)
                    .badgeType(type)
                    .description(type.getDescription())
                    .build();
            achievementRepository.save(a);
            log.info("Achievement awarded: {} → {}", userId, type);
        }
    }
}
