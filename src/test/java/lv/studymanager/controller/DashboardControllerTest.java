package lv.studymanager.controller;

import lv.studymanager.dto.Dto.DashboardStats;
import lv.studymanager.model.Achievement;
import lv.studymanager.model.User;
import lv.studymanager.security.CustomUserDetailsService;
import lv.studymanager.security.JwtTokenProvider;
import lv.studymanager.service.AchievementService;
import lv.studymanager.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private DashboardService dashboardService;
    @MockBean  private AchievementService achievementService;
    @MockBean  private JwtTokenProvider jwtTokenProvider;
    @MockBean  private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "test@test.lv")
    void getStats_returns200() throws Exception {
        DashboardStats stats = new DashboardStats();
        stats.setTotalTasks(10);
        stats.setDoneTasks(5);

        when(dashboardService.getStats("test@test.lv")).thenReturn(stats);

        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTasks").value(10))
            .andExpect(jsonPath("$.doneTasks").value(5));
    }

    @Test
    void getStats_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void getAchievements_returns200() throws Exception {
        User user = User.builder().id(1L).build();
        Achievement a = Achievement.builder()
            .id(1L).user(user)
            .badgeType(Achievement.BadgeType.FIRST_TASK)
            .build();

        when(achievementService.getAchievements("test@test.lv")).thenReturn(List.of(a));

        mockMvc.perform(get("/api/achievements"))
            .andExpect(status().isOk());
    }
}
