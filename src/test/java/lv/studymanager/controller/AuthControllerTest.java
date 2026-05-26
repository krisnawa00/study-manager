package lv.studymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import lv.studymanager.TestSecurityConfig;
import lv.studymanager.dto.Dto.*;
import lv.studymanager.security.CustomUserDetailsService;
import lv.studymanager.security.JwtTokenProvider;
import lv.studymanager.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService authService;
    @MockBean  private JwtTokenProvider jwtTokenProvider;
    @MockBean  private CustomUserDetailsService userDetailsService;

    @Test
    void login_validCredentials_returns200() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.lv");
        req.setPassword("password123");

        UserResponse user = new UserResponse();
        user.setEmail("test@test.lv");
        AuthResponse auth = new AuthResponse("access", "refresh", user);

        when(authService.login(any())).thenReturn(auth);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.lv");
        req.setPassword("wrong");

        when(authService.login(any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@test.lv");
        req.setPassword("password123");
        req.setFirstName("Jānis");
        req.setLastName("Bērziņš");

        UserResponse user = new UserResponse();
        user.setEmail("new@test.lv");
        AuthResponse auth = new AuthResponse("access", "refresh", user);

        when(authService.register(any())).thenReturn(auth);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@test.lv");
        req.setPassword("password123");
        req.setFirstName("A");
        req.setLastName("B");

        when(authService.register(any()))
            .thenThrow(new IllegalArgumentException("E-pasts jau reģistrēts"));

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_validToken_returns200() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("valid-refresh");

        UserResponse user = new UserResponse();
        user.setEmail("test@test.lv");
        AuthResponse auth = new AuthResponse("new-access", "new-refresh", user);

        when(authService.refresh("valid-refresh")).thenReturn(auth);

        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }
}