package site.delivra.application.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import site.delivra.application.model.enums.DeliveryTaskStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "delivery_tasks")
public class DeliveryTask {

    public static final String ID_FIELD = "id";
    public static final String USER_FIELD = "user";
    public static final String STATUS_FIELD = "status";
    public static final String ADDRESS_FIELD = "address";
    public static final String CREATED_FIELD = "created";
    public static final String CREATED_BY_FIELD = "createdBy";
    public static final String DELETED_FIELD = "deleted";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryTaskStatus status;

    @Column(nullable = false, length = 255)
    private String address;

    @Column()
    private Double latitude;

    @Column()
    private Double longitude;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updated;

    @Column(nullable = false)
    private boolean deleted;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;


}
