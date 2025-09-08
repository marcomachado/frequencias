package com.petsaudedigital.backend.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;

@Converter(autoApply = false)
public class InstantStringConverter implements AttributeConverter<Instant, String> {
    @Override
    public String convertToDatabaseColumn(Instant attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public Instant convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Instant.parse(dbData);
    }
}

