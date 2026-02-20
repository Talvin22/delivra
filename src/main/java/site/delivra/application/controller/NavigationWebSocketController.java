package site.delivra.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import site.delivra.application.model.dto.navigation.DriverPositionRequest;
import site.delivra.application.model.dto.navigation.NavigationEventDTO;
import site.delivra.application.service.NavigationService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NavigationWebSocketController {

    private final NavigationService navigationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Driver sends current GPS position.
     *
     * Client publishes to:  /app/navigation/{sessionId}/position
     * Server broadcasts to:
     *   /topic/navigation/{taskId}/position  — when driver is on route
     *   /topic/navigation/{taskId}/route     — when driver is off route (new route included)
     */
    @MessageMapping("/navigation/{sessionId}/position")
    public void handlePositionUpdate(
            @DestinationVariable Integer sessionId,
            DriverPositionRequest positionRequest) {

        log.debug("Position update: sessionId={}, lat={}, lng={}",
                sessionId, positionRequest.getLatitude(), positionRequest.getLongitude());

        NavigationEventDTO event = navigationService.handlePositionUpdate(sessionId, positionRequest);

        if (event.getType() == NavigationEventDTO.Type.POSITION) {
            messagingTemplate.convertAndSend(
                    "/topic/navigation/" + event.getTaskId() + "/position", event);
        } else {
            messagingTemplate.convertAndSend(
                    "/topic/navigation/" + event.getTaskId() + "/route", event);
        }
    }
}
