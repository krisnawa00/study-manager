package lv.studymanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String instructor;

    @Column
    private Integer credits;

    @Column(length = 7)
    @Builder.Default
    private String color = "#2E75B6";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Category category = Category.MANDATORY;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    public enum Status {
        ACTIVE, COMPLETED, FROZEN
    }

    public enum Category {
        MANDATORY, ELECTIVE, EXTRA
    }

    public double getCompletionPercent() {
        if (tasks.isEmpty()) return 0.0;
        long done = tasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count();
        return (double) done / tasks.size() * 100.0;
    }
}
