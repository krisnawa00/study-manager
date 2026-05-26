package lv.studymanager.service;

import lv.studymanager.model.*;
import lv.studymanager.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AchievementServiceTest {

    @Mock private AchievementRepository achievementRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private AchievementService achievementService;

    @Test
    void getAchievements_returnsList() {
        User user = User.builder().id(1L).email("test@test.lv").build();
        Achievement a = Achievement.builder()
            .id(1L).user(user).badgeType(Achievement.BadgeType.FIRST_TASK).build();

        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));
        when(achievementRepository.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(List.of(a));

        List<Achievement> result = achievementService.getAchievements("test@test.lv");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBadgeType()).isEqualTo(Achievement.BadgeType.FIRST_TASK);
    }

    @Test
    void checkAndAward_firstTask_awardsFirstTaskBadge() {
        User user = User.builder().id(1L).build();

        when(taskRepository.countAllByUserId(1L)).thenReturn(1);
        when(taskRepository.countDoneByUserId(1L)).thenReturn(0);
        when(achievementRepository.existsByUserIdAndBadgeType(1L, Achievement.BadgeType.FIRST_TASK))
            .thenReturn(false);
        when(achievementRepository.existsByUserIdAndBadgeType(1L, Achievement.BadgeType.CENTURY))
            .thenReturn(false);
        when(achievementRepository.existsByUserIdAndBadgeType(1L, Achievement.BadgeType.EARLY_BIRD))
            .thenReturn(false);
        when(userRepository.getReferenceById(1L)).thenReturn(user);

        achievementService.checkAndAwardAchievements(1L);

        verify(achievementRepository, atLeastOnce()).save(any());
    }

    @Test
    void checkAndAward_alreadyHasBadge_doesNotAwardAgain() {
        when(taskRepository.countAllByUserId(1L)).thenReturn(1);
        when(taskRepository.countDoneByUserId(1L)).thenReturn(0);
        when(achievementRepository.existsByUserIdAndBadgeType(1L, Achievement.BadgeType.FIRST_TASK))
            .thenReturn(true);
        when(achievementRepository.existsByUserIdAndBadgeType(1L, Achievement.BadgeType.CENTURY))
            .thenReturn(false);
        when(achievementRepository.existsByUserIdAndBadgeType(1L, Achievement.BadgeType.EARLY_BIRD))
            .thenReturn(false);

        achievementService.checkAndAwardAchievements(1L);

        verify(achievementRepository, never()).save(any());
    }
}
