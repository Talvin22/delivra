package site.delivra.application.model.request.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateTruckProfileRequest {

    @Min(value = 1000, message = "Gross weight must be at least 1000 kg")
    @Max(value = 100000, message = "Gross weight cannot exceed 100000 kg")
    private Integer grossWeight;

    @Min(value = 100, message = "Height must be at least 100 cm")
    @Max(value = 600, message = "Height cannot exceed 600 cm")
    private Integer height;

    @Min(value = 100, message = "Width must be at least 100 cm")
    @Max(value = 350, message = "Width cannot exceed 350 cm")
    private Integer width;

    @Min(value = 200, message = "Length must be at least 200 cm")
    @Max(value = 2500, message = "Length cannot exceed 2500 cm")
    private Integer length;
}
