package com.jiaul.jpahelper.utility;

import jakarta.persistence.metamodel.EntityType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

public class Converter {

    public static Object convertValue(String fieldName, String value, EntityType<?> entityType) {
        Class<?> type = entityType.getAttribute(fieldName).getJavaType();
        return convert(value, type);
    }

    public static Object convert(String value, Class<?> type) {

        if (type == Long.class || type == long.class) return Long.valueOf(value);
        if (type == Integer.class || type == int.class) return Integer.valueOf(value);
        if (type == Double.class || type == double.class) return Double.valueOf(value);
        if (type == Float.class || type == float.class) return Float.valueOf(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.valueOf(value);
        if (type.isEnum()) return Enum.valueOf((Class<? extends Enum>) type, value);
        if (type == BigDecimal.class) return new BigDecimal(value);
        if (type == BigInteger.class) return new BigInteger(value);
        if (type == LocalDate.class) return LocalDate.parse(value);
        if (type == LocalDateTime.class) return LocalDateTime.parse(value);
        if (type == LocalTime.class) return LocalTime.parse(value);
        if (type == Date.class) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value);
            } catch (ParseException e) {
                return value;
            }
        }
        if (type == Instant.class) return Instant.parse(value);
        if (type == UUID.class) return UUID.fromString(value);

        return value;
    }
}
