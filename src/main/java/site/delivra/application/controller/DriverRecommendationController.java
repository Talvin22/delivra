package site.delivra.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.delivra.application.model.dto.recommendation.DriverRecommendationDTO;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.service.DriverRecommendationService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class DriverRecommendationController {

    private final DriverRecommendationService driverRecommendationService;

    @GetMapping("/{taskId}/drivers/recommend")
    public ResponseEntity<DelivraResponse<List<DriverRecommendationDTO>>> recommendDrivers(
            @PathVariable Integer taskId,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        DelivraResponse<List<DriverRecommendationDTO>> response =
                driverRecommendationService.recommendDrivers(taskId, limit);
        return ResponseEntity.ok(response);
    }
}
