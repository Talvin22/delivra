package site.delivra.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.delivra.application.model.constants.ApiLogMessage;
import site.delivra.application.model.dto.user.TruckProfileDTO;
import site.delivra.application.model.dto.user.UserDTO;
import site.delivra.application.model.dto.user.UserSearchDTO;
import site.delivra.application.model.request.user.NewUserRequest;
import site.delivra.application.model.request.user.UpdateTruckProfileRequest;
import site.delivra.application.model.request.user.UpdateUserRequest;
import site.delivra.application.model.request.user.UpdateUserRolesRequest;
import site.delivra.application.model.request.user.UserSearchRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.service.UserService;
import site.delivra.application.utils.ApiUtils;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<DelivraResponse<UserDTO>> getUserById(@Valid @PathVariable(name = "id") Integer userId) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<UserDTO> byId = userService.getById(userId);
        return ResponseEntity.ok(byId);
    }

    @PostMapping("/create")
    public ResponseEntity<DelivraResponse<UserDTO>> createUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        DelivraResponse<UserDTO> byId = userService.createUser(newUserRequest);
        return ResponseEntity.ok(byId);


    }

    @PutMapping("/{id}")
    public ResponseEntity<DelivraResponse<UserDTO>> updateUserById(
            @PathVariable(name = "id") Integer userId,
            @RequestBody @Valid UpdateUserRequest updateUserRequest
    ) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<UserDTO> updatedUser = userService.updateUserById(userId, updateUserRequest);
        return ResponseEntity.ok(updatedUser);
    }


    @PutMapping("/{id}/roles")
    public ResponseEntity<DelivraResponse<UserDTO>> updateUserRoles(
            @PathVariable(name = "id") Integer userId,
            @RequestBody @Valid UpdateUserRolesRequest request
    ) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        DelivraResponse<UserDTO> updated = userService.updateUserRoles(userId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DelivraResponse<UserDTO>> softDeleteUser(@PathVariable(name = "id") Integer userId) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        userService.softDeleteUser(userId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/all")
    public ResponseEntity<DelivraResponse<PaginationResponse<UserSearchDTO>>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);
        DelivraResponse<PaginationResponse<UserSearchDTO>> response = userService.findAllUsers(pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me/truck-profile")
    public ResponseEntity<DelivraResponse<TruckProfileDTO>> getTruckProfile() {
        return ResponseEntity.ok(userService.getTruckProfile());
    }

    @PutMapping("/me/truck-profile")
    public ResponseEntity<DelivraResponse<TruckProfileDTO>> updateTruckProfile(
            @RequestBody @Valid UpdateTruckProfileRequest request) {
        return ResponseEntity.ok(userService.updateTruckProfile(request));
    }

    @PostMapping("/search")
    public ResponseEntity<DelivraResponse<PaginationResponse<UserSearchDTO>>> searchUsers(
            @RequestBody @Valid UserSearchRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit

    ) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);
        DelivraResponse<PaginationResponse<UserSearchDTO>> response = userService.searchUsers(request, pageable);
        return ResponseEntity.ok(response);
    }
}
