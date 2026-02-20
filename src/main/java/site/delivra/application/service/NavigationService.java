package site.delivra.application.service;

import site.delivra.application.model.dto.navigation.DriverPositionRequest;
import site.delivra.application.model.dto.navigation.NavigationEventDTO;
import site.delivra.application.model.dto.navigation.NavigationSessionDTO;
import site.delivra.application.model.request.navigation.StartNavigationRequest;
import site.delivra.application.model.response.DelivraResponse;

public interface NavigationService {

    DelivraResponse<NavigationSessionDTO> startNavigation(Integer taskId, StartNavigationRequest request);

    DelivraResponse<NavigationSessionDTO> endNavigation(Integer taskId);

    DelivraResponse<NavigationSessionDTO> getActiveSession(Integer taskId);

    NavigationEventDTO handlePositionUpdate(Integer sessionId, DriverPositionRequest positionRequest);
}
