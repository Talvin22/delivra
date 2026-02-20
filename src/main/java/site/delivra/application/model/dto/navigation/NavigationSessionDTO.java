package site.delivra.application.model.dto.navigation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.delivra.application.model.dto.RouteDTO;
import site.delivra.application.model.enums.NavigationSessionStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NavigationSessionDTO implements Serializable {

    private Integer sessionId;
    private Integer taskId;
    private NavigationSessionStatus status;
    private LocalDateTime startedAt;
    private RouteDTO route;
}
