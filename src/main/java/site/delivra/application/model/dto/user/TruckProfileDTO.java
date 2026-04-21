package site.delivra.application.model.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TruckProfileDTO {
    private Integer grossWeight;
    private Integer height;
    private Integer width;
    private Integer length;
}
