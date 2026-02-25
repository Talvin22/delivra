package site.delivra.application.model.dto.recommendation;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class DriverRecommendationDTO implements Serializable {

    private Integer driverId;
    private String driverUsername;
    private String driverEmail;

    private double totalScore;

    private double proximityScore;
    private double workloadScore;
    private double successRateScore;
    private double recencyScore;

    private Double distanceMeters;
    private long pendingTasksCount;
    private double successRate;
    private Long hoursSinceLastActivity;
}
