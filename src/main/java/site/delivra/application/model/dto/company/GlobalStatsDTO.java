package site.delivra.application.model.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatsDTO implements Serializable {
    private long totalCompanies;
    private long activeCompanies;
    private long trialCompanies;
    private long suspendedCompanies;

    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;

    private long totalUsers;
    private long totalNavigations;
}
