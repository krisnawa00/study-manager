package lv.studymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.Task;
import lv.studymanager.security.CustomUserDetailsService;
import lv.studymanager.security.JwtTokenProvider;
import lv.studymanager.service.TaskService;
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

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private TaskService taskService;
    @MockBean  private JwtTokenProvider jwtTokenProvider;
    @MockBean  private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "test@test.lv")
    void getAllTasks_returns200() throws Exception {
        TaskResponse task = new TaskResponse();
        task.setId(1L);
        task.setTitle("Test task");
        task.setStatus(Task.Status.TODO);

        when(taskService.getAllTasks("test@test.lv")).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Test task"));
    }

    @Test
    void getAllTasks_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void getDueToday_returns200() throws Exception {
        when(taskService.getDueToday("test@test.lv")).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/due-today"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void getOverdue_returns200() throws Exception {
        when(taskService.getOverdue("test@test.lv")).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/overdue"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void createTask_validRequest_returns201() throws Exception {
        TaskRequest req = new TaskRequest();
        req.setTitle("New task");

        TaskResponse res = new TaskResponse();
        res.setId(1L);
        res.setTitle("New task");

        when(taskService.createTask(eq("test@test.lv"), any())).thenReturn(res);

        mockMvc.perform(post("/api/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New task"));
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void createTask_blankTitle_returns400() throws Exception {
        TaskRequest req = new TaskRequest();
        req.setTitle("");

        mockMvc.perform(post("/api/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void updateStatus_returns200() throws Exception {
        TaskResponse res = new TaskResponse();
        res.setId(1L);
        res.setStatus(Task.Status.DONE);

        when(taskService.updateStatus("test@test.lv", 1L, Task.Status.DONE)).thenReturn(res);

        mockMvc.perform(patch("/api/tasks/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @WithMockUser(username = "test@test.lv")
    void deleteTask_returns204() throws Exception {
        doNothing().when(taskService).deleteTask("test@test.lv", 1L);

        mockMvc.perform(delete("/api/tasks/1").with(csrf()))
            .andExpect(status().isNoContent());
    }
}
