package site.delivra.application.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import site.delivra.application.model.enums.NavigationSessionStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "navigation_sessions")
public class NavigationSession {

    public static final String ID_FIELD = "id";
    public static final String DELIVERY_TASK_FIELD = "deliveryTask";
    public static final String STATUS_FIELD = "status";
    public static final String STARTED_AT_FIELD = "startedAt";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_task_id", nullable = false)
    private DeliveryTask deliveryTask;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "encoded_polyline", columnDefinition = "TEXT")
    private String encodedPolyline;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NavigationSessionStatus status = NavigationSessionStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime created = LocalDateTime.now();

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updated = LocalDateTime.now();
}
