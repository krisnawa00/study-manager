package lv.studymanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lv.studymanager.model.Task;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Dto {

    // ─── Auth ───────────────────────────────────────────────────────────────

    @Data
    public static class RegisterRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @Email @NotBlank private String email;
        @NotBlank @Size(min = 8, max = 100) private String password;
    }

    @Data
    public static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private UserResponse user;

        public AuthResponse(String accessToken, String refreshToken, UserResponse user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }

    @Data
    public static class RefreshRequest {
        @NotBlank private String refreshToken;
    }

    // ─── User ────────────────────────────────────────────────────────────────

    @Data
    public static class UserResponse {
        private String role;
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String avatarUrl;
        private LocalDateTime createdAt;
    }
    
    @Data
    public static class AdminUserResponse {
        private Long          id;
        private String        firstName;
        private String        lastName;
        private String        email;
        private String        avatarUrl;
        private String        role;
        private int           totalCourses;
        private int           totalTasks;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class AdminStatsResponse {
        private long   totalUsers;
        private long   totalCourses;
        private long   totalTasks;
        private long   doneTasks;
        private long   overdueTasks;
        private double platformCompletionRate;
    }

    @Data
    public static class ChangeRoleRequest {
        @NotBlank
        private String role;   // "STUDENT" or "ADMIN"
    }
    @Data
    public static class UpdateProfileRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        private String avatarUrl;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank @Size(min = 8) private String newPassword;
    }

    // ─── Course ──────────────────────────────────────────────────────────────

    @Data
    public static class CourseRequest {
        @NotBlank private String name;
        private String instructor;
        private Integer credits;
        private String color;
        private String status;
        private String category;
        private String description;
    }

    @Data
    public static class CourseResponse {
        private Long id;
        private String name;
        private String instructor;
        private Integer credits;
        private String color;
        private String status;
        private String category;
        private String description;
        private double completionPercent;
        private int totalTasks;
        private int doneTasks;
    }

    // ─── Task ────────────────────────────────────────────────────────────────

    @Data
    public static class TaskRequest {
        @NotBlank private String title;
        private String description;
        private Task.Priority priority;
        private LocalDate dueDate;
        private Long courseId;
        private boolean recurring;
        private String recurrencePattern;
    }

    @Data
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private Task.Priority priority;
        private Task.Status status;
        private LocalDate dueDate;
        private boolean overdue;
        private Long courseId;
        private String courseName;
        private String courseColor;
        private LocalDateTime createdAt;
    }

    @Data
    public static class StatusUpdateRequest {
        private Task.Status status;
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    @Data
    public static class DashboardStats {
        private int totalCourses;
        private int activeCourses;
        private int totalTasks;
        private int doneTasks;
        private int overdueTasks;
        private int dueTodayTasks;
        private int achievements;
        private double overallProgress;
    }
}
