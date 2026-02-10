package site.delivra.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.enums.DeliveryTaskStatus;
import site.delivra.application.model.request.task.NewDeliveryTaskRequest;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DeliveryTaskStatus.class, Object.class}
)
public interface DeliveryTaskMapper {

    @Mapping(source = "user.id", target = "userId")
    DeliveryTaskDTO toDto(DeliveryTask deliveryTask);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", expression = "java(DeliveryTaskStatus.PENDING)")
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    DeliveryTask createDeliveryTask(NewDeliveryTaskRequest deliveryTaskRequest);

}
