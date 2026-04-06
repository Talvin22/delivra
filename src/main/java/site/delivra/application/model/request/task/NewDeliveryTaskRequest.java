package site.delivra.application.model.request.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewDeliveryTaskRequest implements Serializable {

    private Integer driverId;

    @NotBlank(message = "address cannot be empty")
    @Size(max = 500, message = "address too long")
    private String address;

    private Double latitude;

    private Double longitude;



}
