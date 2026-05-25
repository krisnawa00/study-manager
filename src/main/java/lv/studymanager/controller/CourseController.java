package lv.studymanager.controller;

import jakarta.validation.Valid;
import lv.studymanager.dto.Dto.*;
import lv.studymanager.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAll(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(courseService.getAllCourses(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getOne(@AuthenticationPrincipal UserDetails user,
                                                  @PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourse(user.getUsername(), id));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> create(@AuthenticationPrincipal UserDetails user,
                                                  @Valid @RequestBody CourseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(user.getUsername(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> update(@AuthenticationPrincipal UserDetails user,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody CourseRequest req) {
        return ResponseEntity.ok(courseService.updateCourse(user.getUsername(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user,
                                        @PathVariable Long id) {
        courseService.deleteCourse(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
