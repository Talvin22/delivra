package site.delivra.application.model.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.delivra.application.service.model.DelivraServiceUserRole;
import site.delivra.application.utils.enum_converter.UserRoleTypeConverter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @Size(max = 50)
    private String name;

    @Column(name = "user_system_role", nullable = false)
    @Convert(converter = UserRoleTypeConverter.class)
    private DelivraServiceUserRole userSystemRole;

    @Column()
    private boolean active;

    @Column(name = "created_by")
    private String createdBy;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "roles")
    private Set<User> users = new HashSet<>();


}
