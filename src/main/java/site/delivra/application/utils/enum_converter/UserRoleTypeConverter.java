package com.post_hub.iam_service.utils.enum_converter;

import com.post_hub.iam_service.service.model.IamServiceUserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserRoleTypeConverter implements AttributeConverter<IamServiceUserRole, String> {

    @Override
    public String convertToDatabaseColumn(IamServiceUserRole attribute) {
        return attribute.name();
    }

    @Override
    public IamServiceUserRole convertToEntityAttribute(String dbData) {
        return IamServiceUserRole.fromName(dbData);
    }
}
