package site.delivra.application.service.impl;

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
import site.delivra.application.exception.DataExistException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.mapper.UserMapper;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.dto.user.UserDTO;
import site.delivra.application.model.dto.user.UserSearchDTO;
import site.delivra.application.model.entities.Role;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.request.user.NewUserRequest;
import site.delivra.application.model.request.user.UpdateUserRequest;
import site.delivra.application.model.request.user.UserSearchRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.repository.RoleRepository;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.repository.criteria.UserSearchCriteria;
import site.delivra.application.security.validation.AccessValidator;
import site.delivra.application.service.UserService;
import site.delivra.application.service.model.DelivraServiceUserRole;

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

    @Override
    public DelivraResponse<UserDTO> getById(@NotNull Integer id) {
        User userById = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));
        UserDTO dto = userMapper.toDto(userById);

        return DelivraResponse.createSuccessful(dto);
    }

    @Override
    public DelivraResponse<UserDTO> createUser(@NotNull NewUserRequest newUserRequest) {
        if (userRepository.existsByUsername(newUserRequest.getUsername())) {
            throw new DataExistException(ApiErrorMessage.USERNAME_ALREADY_EXISTS.getMessage(newUserRequest.getUsername()));
        }

        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(newUserRequest.getEmail()));
        }

        Role role = roleRepository.findByName(DelivraServiceUserRole.USER.getRole())
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_ROLE_NOT_FOUND.getMessage()));

        User user = userMapper.createUser(newUserRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        UserDTO dto = userMapper.toDto(savedUser);

        return DelivraResponse.createSuccessful(dto);

    }

    @Override
    public DelivraResponse<UserDTO> updateUserById(@NotNull Integer userId, UpdateUserRequest request) {
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

       return DelivraResponse.createSuccessful(dto);
    }

    @Override
    public void softDeleteUser(Integer id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));

        accessValidator.validateAdminOrOwnerAccess(user.getId());

        user.setDeleted(true);
        userRepository.save(user);

    }

    @Override
    public DelivraResponse<PaginationResponse<UserSearchDTO>> findAllUsers(Pageable pageable) {
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
        return DelivraResponse.createSuccessful(response);
    }

    @Override
    public DelivraResponse<PaginationResponse<UserSearchDTO>> searchUsers(UserSearchRequest userSearchRequest, Pageable pageable) {
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

        return DelivraResponse.createSuccessful(response);
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

