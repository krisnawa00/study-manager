package lv.studymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import lv.studymanager.TestSecurityConfig;
import lv.studymanager.dto.Dto.*;
import lv.studymanager.security.CustomUserDetailsService;
import lv.studymanager.security.JwtTokenProvider;
import lv.studymanager.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(TestSecurityConfig.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AdminService adminService;
    @MockBean  private JwtTokenProvider jwtTokenProvider;
    @MockBean  private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "admin@test.lv", roles = "ADMIN")
    void getStats_adminRole_returns200() throws Exception {
        AdminStatsResponse stats = new AdminStatsResponse();
        stats.setTotalUsers(10);

        when(adminService.getPlatformStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalUsers").value(10));
    }


    @Test
    @WithMockUser(username = "admin@test.lv", roles = "ADMIN")
    void getAllUsers_returns200() throws Exception {
        AdminUserResponse user = new AdminUserResponse();
        user.setEmail("user@test.lv");

        when(adminService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").value("user@test.lv"));
    }

    @Test
    @WithMockUser(username = "admin@test.lv", roles = "ADMIN")
    void changeRole_returns200() throws Exception {
        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRole("ADMIN");

        AdminUserResponse res = new AdminUserResponse();
        res.setRole("ADMIN");

        when(adminService.changeRole(eq(1L), any())).thenReturn(res);

        mockMvc.perform(patch("/api/admin/users/1/role")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin@test.lv", roles = "ADMIN")
    void deleteUser_returns204() throws Exception {
        doNothing().when(adminService).deleteUser(1L, "admin@test.lv");

        mockMvc.perform(delete("/api/admin/users/1").with(csrf()))
            .andExpect(status().isNoContent());
    }
}