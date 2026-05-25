package lv.studymanager.controller;

import lv.studymanager.dto.Dto.DashboardStats;
import lv.studymanager.model.Achievement;
import lv.studymanager.service.AchievementService;
import lv.studymanager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AchievementService achievementService;

    @GetMapping("/api/dashboard")
    public ResponseEntity<DashboardStats> stats(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(dashboardService.getStats(user.getUsername()));
    }

    @GetMapping("/api/achievements")
    public ResponseEntity<List<Achievement>> achievements(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(achievementService.getAchievements(user.getUsername()));
    }
}
