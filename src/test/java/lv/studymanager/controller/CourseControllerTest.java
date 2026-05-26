package lv.studymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lv.studymanager.dto.Dto.*;
import lv.studymanager.security.CustomUserDetailsService;
import lv.studymanager.security.JwtTokenProvider;
import lv.studymanager.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private CourseService courseService;
    @MockBean  private JwtTokenProvider jwtTokenProvider;
    @MockBean  private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "test@test.lv")
    void getAllCourses_returns200() throws Exception {
        CourseResponse course = new CourseResponse();
        course.setId(1L);
        course.setName("Math");

        when(courseService.getAllCourses("test@test.lv")).thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Math"));
    }

    @Test
    void getAllCourses_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/courses"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void getCourse_returns200() throws Exception {
        CourseResponse course = new CourseResponse();
        course.setId(1L);
        course.setName("Math");

        when(courseService.getCourse("test@test.lv", 1L)).thenReturn(course);

        mockMvc.perform(get("/api/courses/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Math"));
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void createCourse_validRequest_returns201() throws Exception {
        CourseRequest req = new CourseRequest();
        req.setName("Physics");

        CourseResponse res = new CourseResponse();
        res.setId(2L);
        res.setName("Physics");

        when(courseService.createCourse(eq("test@test.lv"), any())).thenReturn(res);

        mockMvc.perform(post("/api/courses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Physics"));
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void createCourse_blankName_returns400() throws Exception {
        CourseRequest req = new CourseRequest();
        req.setName("");

        mockMvc.perform(post("/api/courses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void updateCourse_returns200() throws Exception {
        CourseRequest req = new CourseRequest();
        req.setName("Updated");

        CourseResponse res = new CourseResponse();
        res.setId(1L);
        res.setName("Updated");

        when(courseService.updateCourse(eq("test@test.lv"), eq(1L), any())).thenReturn(res);

        mockMvc.perform(put("/api/courses/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void deleteCourse_returns204() throws Exception {
        doNothing().when(courseService).deleteCourse("test@test.lv", 1L);

        mockMvc.perform(delete("/api/courses/1").with(csrf()))
            .andExpect(status().isNoContent());
    }
}
