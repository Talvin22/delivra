package site.delivra.application.model.dto.user;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class TruckProfileDTO implements Serializable {
    private Integer grossWeight;
    private Integer height;
    private Integer width;
    private Integer length;
}
