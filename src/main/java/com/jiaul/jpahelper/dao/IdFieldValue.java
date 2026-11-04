package com.jiaul.jpahelper.dao;

import java.util.Map;

/**
 * Data contract representing a single entity row in heterogeneous (uneven) bulk batch updates.
 * <p>
 * Implemented by application DTOs, patch request objects, or dynamic property maps passed to
 * {@link com.jiaul.jpahelper.service.updater.UpdateHelper#updateBulkUnevenFields(java.util.List)}.
 * <p>
 * Unlike {@link IdValues}, different rows implementing {@code IdFieldValue} may supply different subsets
 * of entity attributes. The update engine generates SQL {@code CASE ... WHEN ... THEN ... ELSE column END}
 * expressions so that unmentioned attributes automatically retain their current database values.
 *
 * <h3>DTO Example:</h3>
 * <pre>{@code
 * public record StudentPatchDto(Long studentId, String email, String phone) implements IdFieldValue {
 *
 *     @Override
 *     public Object getID() {
 *         return studentId;
 *     }
 *
 *     @Override
 *     public Map<String, Object> getFieldValue() {
 *         Map<String, Object> map = new LinkedHashMap<>();
 *         if (email != null) map.put("email", email);
 *         if (phone != null) map.put("phone", phone);
 *         return map;
 *     }
 * }
 *
 * // Batch update all rows in a single atomic SQL query:
 * studentHelper.updateBulkUnevenFields(patchDtoList);
 * }</pre>
 */
public interface IdFieldValue {

    /**
     * Returns the primary key identifier of the target entity row.
     *
     * @return the entity primary key (e.g., {@link Long}, {@link String}, {@link java.util.UUID})
     */
    Object getID();

    /**
     * Returns a map of attribute names to their new values to update for this specific entity row.
     * <p>
     * Keys must represent valid persistent attribute names on the target entity.
     *
     * @return map of attribute names to update values; must not be {@code null} or empty
     */
    Map<String, Object> getFieldValue();
}
