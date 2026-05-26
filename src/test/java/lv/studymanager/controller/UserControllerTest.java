package lv.studymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import lv.studymanager.TestSecurityConfig;
import lv.studymanager.dto.Dto.*;
import lv.studymanager.security.CustomUserDetailsService;
import lv.studymanager.security.JwtTokenProvider;
import lv.studymanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private UserService userService;
    @MockBean  private JwtTokenProvider jwtTokenProvider;
    @MockBean  private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "test@test.lv")
    void getProfile_returns200() throws Exception {
        UserResponse res = new UserResponse();
        res.setEmail("test@test.lv");
        res.setFirstName("Jānis");

        when(userService.getProfile("test@test.lv")).thenReturn(res);

        mockMvc.perform(get("/api/users/profile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test@test.lv"))
            .andExpect(jsonPath("$.firstName").value("Jānis"));
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void updateProfile_returns200() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("Pēteris");
        req.setLastName("Kalniņš");

        UserResponse res = new UserResponse();
        res.setFirstName("Pēteris");

        when(userService.updateProfile(eq("test@test.lv"), any())).thenReturn(res);

        mockMvc.perform(put("/api/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Pēteris"));
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void changePassword_returns204() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("newpassword");

        doNothing().when(userService).changePassword(eq("test@test.lv"), any());

        mockMvc.perform(put("/api/users/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNoContent());
    }
}
