package site.delivra.application.model.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.delivra.application.model.enums.CompanyStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatsDTO implements Serializable {
    private Integer companyId;
    private String companyName;
    private CompanyStatus status;
    private LocalDateTime trialEndsAt;

    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long pendingTasks;
    private long canceledTasks;

    private long totalDrivers;
    private long totalDispatchers;
    private long totalNavigations;
}
