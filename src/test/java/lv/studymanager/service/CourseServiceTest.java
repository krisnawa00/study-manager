package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.*;
import lv.studymanager.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CourseService courseService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.lv").build();
        when(userRepository.findByEmail("test@test.lv")).thenReturn(Optional.of(user));
    }

    @Test
    void getAllCourses_returnsList() {
        Course c1 = Course.builder().id(1L).user(user).name("Math").color("#fff").build();
        Course c2 = Course.builder().id(2L).user(user).name("Physics").color("#000").build();

        when(courseRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(c1, c2));

        List<CourseResponse> result = courseService.getAllCourses("test@test.lv");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Math");
        assertThat(result.get(1).getName()).isEqualTo("Physics");
    }

    @Test
    void getAllCourses_empty_returnsEmptyList() {
        when(courseRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        assertThat(courseService.getAllCourses("test@test.lv")).isEmpty();
    }

    @Test
    void createCourse_success() {
        CourseRequest req = new CourseRequest();
        req.setName("Datorzinātne");
        req.setInstructor("Profesors");
        req.setCredits(4);
        req.setColor("#2E75B6");

        Course saved = Course.builder().id(1L).user(user)
            .name("Datorzinātne").instructor("Profesors")
            .credits(4).color("#2E75B6").build();

        when(courseRepository.save(any())).thenReturn(saved);

        CourseResponse res = courseService.createCourse("test@test.lv", req);

        assertThat(res.getName()).isEqualTo("Datorzinātne");
        assertThat(res.getInstructor()).isEqualTo("Profesors");
        assertThat(res.getCredits()).isEqualTo(4);
    }

    @Test
    void createCourse_noColor_usesDefault() {
        CourseRequest req = new CourseRequest();
        req.setName("Kurss");
        // color is null

        Course saved = Course.builder().id(1L).user(user)
            .name("Kurss").color("#2E75B6").build();

        when(courseRepository.save(any())).thenReturn(saved);

        CourseResponse res = courseService.createCourse("test@test.lv", req);

        assertThat(res.getColor()).isEqualTo("#2E75B6");
    }

    @Test
    void getCourse_success() {
        Course course = Course.builder().id(1L).user(user).name("Math").color("#fff").build();

        when(courseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(course));

        CourseResponse res = courseService.getCourse("test@test.lv", 1L);

        assertThat(res.getName()).isEqualTo("Math");
    }

    @Test
    void getCourse_notOwned_throws() {
        when(courseRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourse("test@test.lv", 99L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Kurss nav atrasts");
    }

    @Test
    void updateCourse_success() {
        Course existing = Course.builder().id(1L).user(user).name("Old").color("#fff").build();

        CourseRequest req = new CourseRequest();
        req.setName("New Name");
        req.setColor("#000");

        when(courseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(courseRepository.save(any())).thenReturn(existing);

        CourseResponse res = courseService.updateCourse("test@test.lv", 1L, req);

        assertThat(res.getName()).isEqualTo("New Name");
    }

    @Test
    void deleteCourse_success() {
        Course course = Course.builder().id(1L).user(user).name("Math").color("#fff").build();

        when(courseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(course));

        courseService.deleteCourse("test@test.lv", 1L);

        verify(courseRepository).delete(course);
    }

    @Test
    void deleteCourse_notOwned_throws() {
        when(courseRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.deleteCourse("test@test.lv", 99L))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
