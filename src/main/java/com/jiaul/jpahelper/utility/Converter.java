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

/**
 * Utility class for converting raw string values into strongly-typed Java objects.
 * <p>
 * Commonly used when converting external string inputs (such as HTTP query parameters or dynamic filter values)
 * into the Java data types expected by JPA entity attributes.
 * <p>
 * Supported conversion types:
 * <ul>
 *   <li><b>Primitive and Boxed Numbers:</b> {@code long}/{@link Long}, {@code int}/{@link Integer},
 *       {@code double}/{@link Double}, {@code float}/{@link Float}, {@link BigDecimal}, {@link BigInteger}</li>
 *   <li><b>Booleans:</b> {@code boolean}/{@link Boolean}</li>
 *   <li><b>Enums:</b> Any {@link Enum} type via {@link Enum#valueOf}</li>
 *   <li><b>Date and Time:</b> {@link LocalDate}, {@link LocalDateTime}, {@link LocalTime}, {@link Instant},
 *       and {@link Date} (formatted as {@code yyyy-MM-dd HH:mm:ss})</li>
 *   <li><b>Identifiers:</b> {@link UUID} via {@link UUID#fromString}</li>
 * </ul>
 */
public final class Converter {

    private Converter() {
        // Prevent direct instantiation
    }

    /**
     * Converts a string value into the target type declared on a JPA entity attribute.
     * <p>
     * Inspects the {@link EntityType} metamodel to discover the Java type of {@code fieldName},
     * then delegates to {@link #convert(String, Class)}.
     *
     * @param fieldName  the entity attribute name; must exist on {@code entityType}
     * @param value      the raw string value to convert
     * @param entityType the JPA metamodel descriptor for the target entity
     * @return the converted object matching the attribute's Java type, or the original string if unhandled
     * @throws IllegalArgumentException if {@code fieldName} is not an attribute of {@code entityType}
     */
    public static Object convertValue(String fieldName, String value, EntityType<?> entityType) {
        Class<?> type = entityType.getAttribute(fieldName).getJavaType();
        return convert(value, type);
    }

    /**
     * Converts a raw string representation into the specified destination class type.
     * <p>
     * If {@code value} is {@code null}, {@code type} is {@code null}, or {@code type} is {@link String},
     * the input {@code value} is returned directly. For unhandled custom classes, {@code value} is returned unchanged.
     *
     * @param value the raw string value to convert; can be {@code null}
     * @param type  the destination {@link Class} type
     * @return the parsed object converted to {@code type}, or {@code value} if no specific conversion applies
     * @throws NumberFormatException if numeric parsing fails
     * @throws java.time.format.DateTimeParseException if date/time parsing fails
     * @throws IllegalArgumentException if enum or UUID parsing fails
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object convert(String value, Class<?> type) {
        if (value == null || type == null || type == String.class) {
            return value;
        }

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
