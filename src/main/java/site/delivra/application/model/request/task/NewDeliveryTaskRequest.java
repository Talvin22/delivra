package site.delivra.application.model.request.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewDeliveryTaskRequest implements Serializable {

    @NotNull(message = "driverId cannot be null")
    private Integer driverId;

    @NotBlank(message = "address cannot be empty")
    private String address;

    private Double latitude;

    private Double longitude;



}
