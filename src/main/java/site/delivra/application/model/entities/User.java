package site.delivra.application.model.entities;



import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.delivra.application.model.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    public static final String ID_FIELD = "id";
    public static final String USERNAME_NAME_FIELD = "username";
    public static final String PASSWORD_NAME_FIELD = "password";
    public static final String EMAIL_NAME_FIELD = "email";
    public static final String CREATED_FIELD = "created";
    public static final String UPDATED_FIELD = "updated";
    public static final String LAST_LOGIN_FIELD = "last_login";
    public static final String DELETED_FIELD = "deleted";
    public static final String STATUS_FIELD = "status";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 30)
    @Column(length = 30, nullable = false, unique = true)
    private String username;

    @Size(max = 80)
    @Column(length = 80, nullable = false)
    private String password;

    @Size(max = 50)
    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDateTime created = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updated = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "registration_status")
    private RegistrationStatus status;

    @ManyToMany()
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "user")
    private Collection<DeliveryTask> deliveryTasks;




}
