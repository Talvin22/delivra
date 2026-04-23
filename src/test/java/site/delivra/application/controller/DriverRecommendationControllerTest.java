package site.delivra.application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import site.delivra.application.advice.GlobalExceptionHandler;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.model.dto.recommendation.DriverRecommendationDTO;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.security.JwtTokenProvider;
import site.delivra.application.service.DriverRecommendationService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {DriverRecommendationController.class, GlobalExceptionHandler.class},
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class DriverRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverRecommendationService driverRecommendationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void recommendDrivers_valid_returnsRankedList() throws Exception {
        DriverRecommendationDTO top = DriverRecommendationDTO.builder()
                .driverId(10)
                .driverUsername("driver_near")
                .driverEmail("near@example.com")
                .totalScore(0.85)
                .proximityScore(0.9)
                .workloadScore(0.8)
                .successRateScore(0.7)
                .recencyScore(0.9)
                .distanceMeters(500.0)
                .pendingTasksCount(1)
                .build();

        ArrayList<DriverRecommendationDTO> recommendations = new ArrayList<>(List.of(top));
        when(driverRecommendationService.recommendDrivers(1, 20))
                .thenReturn(DelivraResponse.createSuccessful(recommendations));

        mockMvc.perform(get("/tasks/1/drivers/recommend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.payload[0].driverId").value(10))
                .andExpect(jsonPath("$.payload[0].driverUsername").value("driver_near"));
    }

    @Test
    void recommendDrivers_withLimitParam_usesLimit() throws Exception {
        when(driverRecommendationService.recommendDrivers(1, 5))
                .thenReturn(DelivraResponse.createSuccessful(new ArrayList<>()));

        mockMvc.perform(get("/tasks/1/drivers/recommend").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").isArray())
                .andExpect(jsonPath("$.payload.length()").value(0));
    }

    @Test
    void recommendDrivers_taskNotFound_returns404() throws Exception {
        when(driverRecommendationService.recommendDrivers(99, 20))
                .thenThrow(new NotFoundException("task not found"));

        mockMvc.perform(get("/tasks/99/drivers/recommend"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("task not found"));
    }
}