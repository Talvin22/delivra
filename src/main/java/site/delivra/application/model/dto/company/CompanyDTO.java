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
public class CompanyDTO implements Serializable {
    private Integer id;
    private String name;
    private String email;
    private CompanyStatus status;
    private LocalDateTime trialEndsAt;
    private LocalDateTime created;
}
