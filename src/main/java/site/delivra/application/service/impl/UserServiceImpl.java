package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.kafka.service.KafkaMessageService;
import com.post_hub.iam_service.mapper.UserMapper;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.dto.user.UserDTO;
import com.post_hub.iam_service.model.dto.user.UserSearchDTO;
import com.post_hub.iam_service.model.entities.Role;
import com.post_hub.iam_service.model.entities.User;
import com.post_hub.iam_service.model.exception.DataExistException;
import com.post_hub.iam_service.model.exception.NotFoundException;
import com.post_hub.iam_service.model.request.user.NewUserRequest;
import com.post_hub.iam_service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_service.model.request.user.UserSearchRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.model.response.PaginationResponse;
import com.post_hub.iam_service.repository.RoleRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.repository.criteria.UserSearchCriteria;
import com.post_hub.iam_service.security.validation.AccessValidator;
import com.post_hub.iam_service.service.UserService;
import com.post_hub.iam_service.service.model.IamServiceUserRole;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AccessValidator accessValidator;
    private final KafkaMessageService kafkaMessageService;

    @Override
    public IamResponse<UserDTO> getById(@NotNull Integer id) {
        User userById = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));
        UserDTO dto = userMapper.toDto(userById);

        return IamResponse.createSuccessful(dto);
    }

    @Override
    public IamResponse<UserDTO> createUser(@NotNull NewUserRequest newUserRequest) {
        if (userRepository.existsByUsername(newUserRequest.getUsername())) {
            throw new DataExistException(ApiErrorMessage.USERNAME_ALREADY_EXISTS.getMessage(newUserRequest.getUsername()));
        }

        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(newUserRequest.getEmail()));
        }

        Role role = roleRepository.findByName(IamServiceUserRole.USER.getRole())
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_ROLE_NOT_FOUND.getMessage()));

        User user = userMapper.createUser(newUserRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        UserDTO dto = userMapper.toDto(savedUser);

        kafkaMessageService.sendUserCreatedMessage(user.getId(), user.getUsername());
        return IamResponse.createSuccessful(dto);

    }

    @Override
    public IamResponse<UserDTO> updateUserById(@NotNull Integer userId, UpdateUserRequest request) {
       User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

       accessValidator.validateAdminOrOwnerAccess(user.getId());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DataExistException(ApiErrorMessage.USERNAME_ALREADY_EXISTS.getMessage(request.getUsername()));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(request.getEmail()));
        }

       userMapper.updateUser(user, request);
       user.setUpdated(LocalDateTime.now());
       user = userRepository.save(user);
       UserDTO dto = userMapper.toDto(user);

       kafkaMessageService.sendUserUpdatedMessage(user.getId(), user.getUsername());
       return IamResponse.createSuccessful(dto);
    }

    @Override
    public void softDeleteUser(Integer id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));

        accessValidator.validateAdminOrOwnerAccess(user.getId());

        user.setDeleted(true);
        kafkaMessageService.sendUserDeletedMessage(user.getId(), user.getUsername());
        userRepository.save(user);

    }

    @Override
    public IamResponse<PaginationResponse<UserSearchDTO>> findAllUsers(Pageable pageable) {
        Page<UserSearchDTO> users = userRepository.findAll(pageable)
                .map(userMapper::toUserSearchDTO);

        PaginationResponse<UserSearchDTO> response = new PaginationResponse<>(
                users.getContent(),
                new PaginationResponse.Pagination(
                        users.getTotalElements(),
                        users.getSize(),
                        users.getNumber() + 1,
                        users.getTotalPages()
                )
        );
        return IamResponse.createSuccessful(response);
    }

    @Override
    public IamResponse<PaginationResponse<UserSearchDTO>> searchUsers(UserSearchRequest userSearchRequest, Pageable pageable) {
        Specification<User> specification = new UserSearchCriteria(userSearchRequest);

        Page<UserSearchDTO> usersPage = userRepository.findAll(specification, pageable)
                .map(userMapper::toUserSearchDTO);

        PaginationResponse<UserSearchDTO> response = PaginationResponse.<UserSearchDTO>builder()
                .content(usersPage.getContent())
                .pagination(PaginationResponse.Pagination.builder()
                        .total(usersPage.getTotalElements())
                        .limit(usersPage.getSize())
                        .page(usersPage.getNumber() + 1)
                        .pages(usersPage.getTotalPages())
                        .build())
                .build();

        return IamResponse.createSuccessful(response);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return getUserDetails(email, userRepository);

    }

    static UserDetails getUserDetails(String email, UserRepository userRepository) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.EMAIL_NOT_FOUND.getMessage(email)));

        user.setLast_login(LocalDateTime.now());
        userRepository.save(user);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList()

        );
    }
}

