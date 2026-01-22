package com.post_hub.iam_service.model.entities;

import com.post_hub.iam_service.service.model.IamServiceUserRole;
import com.post_hub.iam_service.utils.enum_converter.UserRoleTypeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private IamServiceUserRole userSystemRole;

    @Column()
    private boolean active;

    @Column(name = "created_by")
    private String createdBy;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "roles")
    private Set<User> users = new HashSet<>();


}
