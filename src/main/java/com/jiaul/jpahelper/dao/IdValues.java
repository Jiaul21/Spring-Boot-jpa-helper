package com.jiaul.jpahelper.dao;

import java.util.List;

/**
 * Data contract representing a single entity row in uniform (even) bulk batch updates.
 * <p>
 * Implemented by application DTOs, Java records, or data holders passed to
 * {@link com.jiaul.jpahelper.service.updater.UpdateHelper#updateBulkEvenFields(List, List)}.
 * <p>
 * Each implementing instance supplies its primary key identifier via {@link #getID()} and an ordered
 * list of attribute values via {@link #getValues()} matching the order of field names supplied to the update engine.
 *
 * <h3>DTO Example:</h3>
 * <pre>{@code
 * public record StudentGradeDto(Long studentId, String grade, Integer credits) implements IdValues {
 *
 *     @Override
 *     public Object getID() {
 *         return studentId;
 *     }
 *
 *     @Override
 *     public List<Object> getValues() {
 *         return List.of(grade, credits);
 *     }
 * }
 *
 * // Batch update all rows in a single SQL query:
 * studentHelper.updateBulkEvenFields(List.of("grade", "credits"), studentGradeDtoList);
 * }</pre>
 */
public interface IdValues {

    /**
     * Returns the primary key identifier of the target entity row.
     *
     * @return the entity primary key (e.g., {@link Long}, {@link String}, {@link java.util.UUID})
     */
    Object getID();

    /**
     * Returns the ordered list of attribute values for this row.
     * <p>
     * The order and element count of this list must exactly correspond to the ordered list of field names
     * passed to the bulk update method.
     *
     * @return the ordered list of values to update
     */
    List<Object> getValues();
}
