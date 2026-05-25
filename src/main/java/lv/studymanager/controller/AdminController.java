package lv.studymanager.controller;

import jakarta.validation.Valid;
import lv.studymanager.dto.Dto.*;
import lv.studymanager.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // ← blocks non-admins on every method
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getPlatformStats() {
        return ResponseEntity.ok(adminService.getPlatformStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> getUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<AdminUserResponse> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest req) {
        return ResponseEntity.ok(adminService.changeRole(id, req));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails caller) {
        adminService.deleteUser(id, caller.getUsername());
        return ResponseEntity.noContent().build();
    }
}
