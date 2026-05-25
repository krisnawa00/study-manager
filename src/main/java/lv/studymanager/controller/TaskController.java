package lv.studymanager.controller;

import jakarta.validation.Valid;
import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.Task;
import lv.studymanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) Long courseId) {
        if (courseId != null) {
            return ResponseEntity.ok(taskService.getTasksByCourse(user.getUsername(), courseId));
        }
        return ResponseEntity.ok(taskService.getAllTasks(user.getUsername()));
    }

    @GetMapping("/due-today")
    public ResponseEntity<List<TaskResponse>> dueToday(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(taskService.getDueToday(user.getUsername()));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> overdue(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(taskService.getOverdue(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal UserDetails user,
                                                @Valid @RequestBody TaskRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(user.getUsername(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@AuthenticationPrincipal UserDetails user,
                                                @PathVariable Long id,
                                                @Valid @RequestBody TaskRequest req) {
        return ResponseEntity.ok(taskService.updateTask(user.getUsername(), id, req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(@AuthenticationPrincipal UserDetails user,
                                                      @PathVariable Long id,
                                                      @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(taskService.updateStatus(user.getUsername(), id, req.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user,
                                        @PathVariable Long id) {
        taskService.deleteTask(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
