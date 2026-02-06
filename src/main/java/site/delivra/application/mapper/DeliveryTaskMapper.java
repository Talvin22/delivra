package site.delivra.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.enums.DeliveryTaskStatus;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DeliveryTaskStatus.class, Object.class}
)
public interface DeliveryTaskMapper {

    @Mapping(source = "user.id", target = "userId")
    DeliveryTaskDTO toDto(DeliveryTask deliveryTask);

}
