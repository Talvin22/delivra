package site.delivra.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import site.delivra.application.model.enums.DeliveryTaskStatus;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DeliveryTaskStatus.class, Object.class}
)
public interface DeliveryTaskMapper {

}
