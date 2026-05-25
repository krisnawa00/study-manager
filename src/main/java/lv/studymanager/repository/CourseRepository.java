package lv.studymanager.repository;

import lv.studymanager.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Course> findByUserIdAndStatus(Long userId, Course.Status status);
    Optional<Course> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    int countActiveCoursesByUserId(Long userId);
}
