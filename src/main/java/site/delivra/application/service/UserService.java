package site.delivra.application.service;


import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import site.delivra.application.model.dto.user.UserDTO;
import site.delivra.application.model.dto.user.UserSearchDTO;
import site.delivra.application.model.request.user.NewUserRequest;
import site.delivra.application.model.request.user.UpdateUserRequest;
import site.delivra.application.model.request.user.UserSearchRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;

public interface UserService extends UserDetailsService {

    DelivraResponse<UserDTO> getById(@NotNull Integer id);

    DelivraResponse<UserDTO> createUser(@NotNull NewUserRequest newUserRequest);

    DelivraResponse<UserDTO> updateUserById(@NotNull Integer userId, UpdateUserRequest updateUserRequest);

    void softDeleteUser(@NotNull Integer id);

    DelivraResponse<PaginationResponse<UserSearchDTO>> findAllUsers(Pageable pageable);

    DelivraResponse<PaginationResponse<UserSearchDTO>> searchUsers(UserSearchRequest userSerachRequest, Pageable pageable);
}
