package site.delivra.application.model.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.delivra.application.model.enums.DeliveryTaskStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeliveryTaskDTO implements Serializable {

    private Integer id;

    @NotNull(message = "Delivery status cannot be null")
    private DeliveryTaskStatus status;

    @NotBlank(message = "Address cannot be empty")
    private String address;

    @NotNull(message = "latitude cannot be null")
    private Double latitude;

    @NotNull(message = "longitude cannot be null")
    private Double longitude;

    @NotNull(message = "start time cannot be null")
    private LocalDateTime startTime;

    @NotNull(message = "end time cannot be null")
    private LocalDateTime endTime;

    @NotNull(message = "created cannot be null")
    private LocalDateTime created;

    @NotNull(message = "updated cannot be null")
    private LocalDateTime updated;

    @NotNull(message = "created by cannot be null")
    private String createdBy;


}
