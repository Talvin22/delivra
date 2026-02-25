package site.delivra.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.delivra.application.model.constants.ApiLogMessage;
import site.delivra.application.model.dto.navigation.NavigationSessionDTO;
import site.delivra.application.model.request.navigation.StartNavigationRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.service.NavigationService;
import site.delivra.application.utils.ApiUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks/{taskId}/navigation")
public class NavigationController {

    private final NavigationService navigationService;

    @PostMapping("/start")
    public ResponseEntity<DelivraResponse<NavigationSessionDTO>> startNavigation(
            @PathVariable Integer taskId,
            @RequestBody @Valid StartNavigationRequest request) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<NavigationSessionDTO> response = navigationService.startNavigation(taskId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/end")
    public ResponseEntity<DelivraResponse<NavigationSessionDTO>> endNavigation(@PathVariable Integer taskId) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<NavigationSessionDTO> response = navigationService.endNavigation(taskId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<DelivraResponse<NavigationSessionDTO>> getActiveSession(@PathVariable Integer taskId) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<NavigationSessionDTO> response = navigationService.getActiveSession(taskId);
        return ResponseEntity.ok(response);
    }
}
