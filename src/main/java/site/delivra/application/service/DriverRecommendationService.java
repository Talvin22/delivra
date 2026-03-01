package site.delivra.application.service;

import site.delivra.application.model.dto.recommendation.DriverRecommendationDTO;
import site.delivra.application.model.response.DelivraResponse;

import java.util.ArrayList;

public interface DriverRecommendationService {

    DelivraResponse<ArrayList<DriverRecommendationDTO>> recommendDrivers(Integer taskId, int limit);
}
