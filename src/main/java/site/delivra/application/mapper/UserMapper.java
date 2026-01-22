package com.post_hub.iam_service.mapper;

import com.post_hub.iam_service.model.dto.role.RoleDTO;
import com.post_hub.iam_service.model.dto.user.UserDTO;
import com.post_hub.iam_service.model.dto.user.UserProfileDTO;
import com.post_hub.iam_service.model.dto.user.UserSearchDTO;
import com.post_hub.iam_service.model.entities.Role;
import com.post_hub.iam_service.model.entities.User;
import com.post_hub.iam_service.model.enums.RegistrationStatus;
import com.post_hub.iam_service.model.request.user.NewUserRequest;
import com.post_hub.iam_service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_service.model.request.user.UpdateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collection;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {RegistrationStatus.class, Object.class}
)
public interface UserMapper {

    @Mapping(source = "last_login", target = "lastLogin")
    @Mapping(source = "status", target = "registrationStatus")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "status", expression = "java(RegistrationStatus.ACTIVE)")
    User createUser(NewUserRequest newUserRequest);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "token", source = "token")
    @Mapping(target = "refreshToken", source = "refreshToken")
    UserProfileDTO toUserProfileDto(User user, String token, String refreshToken);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "status", expression = "java(RegistrationStatus.ACTIVE)")
    User fromDto(RegistrationUserRequest registrationUserRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    void updateUser(@MappingTarget User user, UpdateUserRequest updateUserRequest);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserSearchDTO toUserSearchDTO(User user);

    default List<RoleDTO> mapRoles(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new RoleDTO(role.getId(), role.getName()))
                .toList();
    }
}
