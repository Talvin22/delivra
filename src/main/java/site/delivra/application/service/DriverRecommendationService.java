package site.delivra.application.service;

import site.delivra.application.model.dto.recommendation.DriverRecommendationDTO;
import site.delivra.application.model.response.DelivraResponse;

import java.util.List;

public interface DriverRecommendationService {

    DelivraResponse<List<DriverRecommendationDTO>> recommendDrivers(Integer taskId, int limit);
}
