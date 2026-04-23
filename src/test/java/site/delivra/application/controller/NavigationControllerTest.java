package site.delivra.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import site.delivra.application.advice.GlobalExceptionHandler;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.model.dto.navigation.NavigationSessionDTO;
import site.delivra.application.model.enums.NavigationSessionStatus;
import site.delivra.application.model.request.navigation.StartNavigationRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.security.JwtTokenProvider;
import site.delivra.application.service.NavigationService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {NavigationController.class, GlobalExceptionHandler.class},
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class NavigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NavigationService navigationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void startNavigation_valid_returns200() throws Exception {
        NavigationSessionDTO dto = NavigationSessionDTO.builder()
                .sessionId(100)
                .taskId(1)
                .status(NavigationSessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .build();
        when(navigationService.startNavigation(eq(1), any(StartNavigationRequest.class)))
                .thenReturn(DelivraResponse.createSuccessful(dto));

        StartNavigationRequest req = new StartNavigationRequest();
        req.setOriginLatitude(55.75);
        req.setOriginLongitude(37.62);

        mockMvc.perform(post("/tasks/1/navigation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.payload.sessionId").value(100))
                .andExpect(jsonPath("$.payload.taskId").value(1));
    }

    @Test
    void startNavigation_taskNotFound_returns404() throws Exception {
        when(navigationService.startNavigation(eq(99), any(StartNavigationRequest.class)))
                .thenThrow(new NotFoundException("task not found"));

        StartNavigationRequest req = new StartNavigationRequest();
        req.setOriginLatitude(55.75);
        req.setOriginLongitude(37.62);

        mockMvc.perform(post("/tasks/99/navigation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("task not found"));
    }

    @Test
    void startNavigation_alreadyActive_returns409() throws Exception {
        when(navigationService.startNavigation(eq(1), any(StartNavigationRequest.class)))
                .thenThrow(new InvalidDataException("task 1 already has an active navigation session"));

        StartNavigationRequest req = new StartNavigationRequest();
        req.setOriginLatitude(55.75);
        req.setOriginLongitude(37.62);

        mockMvc.perform(post("/tasks/1/navigation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void startNavigation_missingOriginLat_returns400() throws Exception {
        String body = "{\"originLongitude\": 37.62}";

        mockMvc.perform(post("/tasks/1/navigation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void startNavigation_latitudeOutOfRange_returns400() throws Exception {
        String body = "{\"originLatitude\": 200, \"originLongitude\": 37.62}";

        mockMvc.perform(post("/tasks/1/navigation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endNavigation_valid_returns200() throws Exception {
        NavigationSessionDTO dto = NavigationSessionDTO.builder()
                .sessionId(100)
                .taskId(1)
                .status(NavigationSessionStatus.COMPLETED)
                .build();
        when(navigationService.endNavigation(1)).thenReturn(DelivraResponse.createSuccessful(dto));

        mockMvc.perform(post("/tasks/1/navigation/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.payload.status").value("COMPLETED"));
    }

    @Test
    void endNavigation_notFound_returns404() throws Exception {
        when(navigationService.endNavigation(99))
                .thenThrow(new NotFoundException("no active session"));

        mockMvc.perform(post("/tasks/99/navigation/end"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActiveSession_valid_returns200() throws Exception {
        NavigationSessionDTO dto = NavigationSessionDTO.builder()
                .sessionId(100)
                .taskId(1)
                .status(NavigationSessionStatus.ACTIVE)
                .build();
        when(navigationService.getActiveSession(1)).thenReturn(DelivraResponse.createSuccessful(dto));

        mockMvc.perform(get("/tasks/1/navigation/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.sessionId").value(100));
    }
}