package lv.studymanager.repository;

import lv.studymanager.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByUserIdOrderByEarnedAtDesc(Long userId);
    boolean existsByUserIdAndBadgeType(Long userId, Achievement.BadgeType badgeType);
    int countByUserId(Long userId);
}
