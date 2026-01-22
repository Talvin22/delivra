package com.post_hub.iam_service.service;


import com.post_hub.iam_service.model.dto.user.UserDTO;
import com.post_hub.iam_service.model.dto.user.UserSearchDTO;
import com.post_hub.iam_service.model.request.user.NewUserRequest;
import com.post_hub.iam_service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_service.model.request.user.UserSearchRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.model.response.PaginationResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    IamResponse<UserDTO> getById(@NotNull Integer id);

    IamResponse<UserDTO> createUser(@NotNull NewUserRequest newUserRequest);

    IamResponse<UserDTO> updateUserById(@NotNull Integer userId, UpdateUserRequest updateUserRequest);

    void softDeleteUser(@NotNull Integer id);

    IamResponse<PaginationResponse<UserSearchDTO>> findAllUsers(Pageable pageable);

    IamResponse<PaginationResponse<UserSearchDTO>> searchUsers(UserSearchRequest userSerachRequest, Pageable pageable);
}
