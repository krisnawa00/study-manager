package lv.studymanager.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeType badgeType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime earnedAt;

    public enum BadgeType {
        FIRST_TASK("Pirmais solis", "Izveidojāt savu pirmo uzdevumu!"),
        WEEK_STREAK("Nedēļas sērija", "Strādājāt 7 dienas pēc kārtas"),
        COURSE_COMPLETE("Kurss pabeigts", "Pabeidzāt kursu ar visiem uzdevumiem"),
        EARLY_BIRD("Agrais putns", "Pabeidzāt uzdevumu 2 dienas pirms termiņa"),
        CENTURY("100 uzdevumi", "Izveidojāt 100 uzdevumus"),
        PERFECT_WEEK("Ideālā nedēļa", "Pabeidzāt visus uzdevumus nedēļā");

        private final String label;
        private final String description;

        BadgeType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }
}
