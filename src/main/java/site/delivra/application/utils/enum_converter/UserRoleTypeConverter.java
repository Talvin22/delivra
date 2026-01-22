package site.delivra.application.utils.enum_converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import site.delivra.application.service.model.DelivraServiceUserRole;

@Converter
public class UserRoleTypeConverter implements AttributeConverter<DelivraServiceUserRole, String> {

    @Override
    public String convertToDatabaseColumn(DelivraServiceUserRole attribute) {
        return attribute.name();
    }

    @Override
    public DelivraServiceUserRole convertToEntityAttribute(String dbData) {
        return DelivraServiceUserRole.fromName(dbData);
    }
}
