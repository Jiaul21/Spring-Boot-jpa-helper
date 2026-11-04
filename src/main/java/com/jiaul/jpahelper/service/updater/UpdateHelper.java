package com.jiaul.jpahelper.service.updater;

import com.jiaul.jpahelper.dao.IdFieldValue;
import com.jiaul.jpahelper.dao.IdValues;
import com.jiaul.jpahelper.service.base.BaseHelper;
import jakarta.persistence.EntityNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Service helper interface providing targeted single-entity updates, conditional batch updates,
 * and high-performance CASE-WHEN bulk update operations.
 * <p>
 * Offers two update paradigms:
 * <ul>
 *   <li><b>Managed Entity Updates:</b> {@link #updateEntity(Object, Consumer)} loads the managed entity, executes in-place mutations,
 *       and lets JPA dirty checking handle persistence.</li>
 *   <li><b>Direct Bulk JPQL Updates:</b> Methods such as {@link #updateFieldByID}, {@link #updateFieldsByIDIn},
 *       {@link #updateBulkEvenFields}, and {@link #updateBulkUnevenFields} execute atomic SQL/JPQL updates directly against
 *       the database without loading entities into memory, automatically evicting the persistence context to avoid stale cache.</li>
 * </ul>
 *
 * @param <E>  the managed domain entity type
 * @param <ID> the primary key identifier type of the entity
 */
public interface UpdateHelper<E, ID> extends BaseHelper<E, ID> {

    // -------------------------------------------------------------
    // Managed In-Place Updates
    // -------------------------------------------------------------

    /**
     * Loads the entity by its identifier, applies the given mutation consumer within an active transaction,
     * and returns the managed entity.
     *
     * @param id      the primary key identifier of the entity to update; must not be {@code null}
     * @param updater consumer lambda defining modifications to apply to the managed entity; must not be {@code null}
     * @return the modified, managed entity instance
     * @throws NullPointerException    if {@code id} or {@code updater} is {@code null}
     * @throws EntityNotFoundException if no entity is found matching {@code id}
     */
    E updateEntity(ID id, Consumer<E> updater);

    // -------------------------------------------------------------
    // Direct Bulk JPQL Updates by ID
    // -------------------------------------------------------------

    /**
     * Updates a single attribute on a specific entity directly via JPQL without loading the entity into memory.
     * <p>
     * Clears the persistence context upon successful execution if rows were modified.
     *
     * @param id        the target entity identifier; must not be {@code null}
     * @param fieldName the entity attribute name to update; must not be {@code null} or blank
     * @param value     the new attribute value to set (can be {@code null})
     * @return the number of entities updated (typically {@code 1} if found, {@code 0} otherwise)
     * @throws IllegalArgumentException if {@code id} is {@code null} or {@code fieldName} is {@code null} or blank
     */
    int updateFieldByID(ID id, String fieldName, Object value);

    /**
     * Updates multiple attributes across a collection of entity identifiers in a single JPQL query.
     * <p>
     * Clears the persistence context upon successful execution if rows were modified.
     *
     * @param ids    collection of target entity identifiers; must not be {@code null} or empty
     * @param fields map of entity attribute names to their new values; must not be {@code null} or empty
     * @return the total number of entities updated
     * @throws IllegalArgumentException if {@code ids} or {@code fields} is {@code null} or empty
     */
    int updateFieldsByIDIn(Collection<ID> ids, Map<String, Object> fields);

    // -------------------------------------------------------------
    // Conditional Direct Updates
    // -------------------------------------------------------------

    /**
     * Updates a single attribute on all entities matching a single condition expression.
     * <p>
     * If {@code cValue} is {@code null}, an {@code IS NULL} condition is applied.
     * Clears the persistence context upon successful execution if rows were modified.
     *
     * @param fieldName  the entity attribute name to update; must not be {@code null} or blank
     * @param fieldValue the new attribute value to set
     * @param cName      the condition attribute name to filter by; must not be {@code null} or blank
     * @param cValue     the expected condition value (or {@code null} to match {@code NULL})
     * @return the number of entities updated
     * @throws IllegalArgumentException if {@code fieldName} or {@code cName} is {@code null} or blank
     */
    int updateField(String fieldName, Object fieldValue, String cName, Object cValue);

    /**
     * Updates multiple attributes on all entities matching multiple {@code AND} condition criteria.
     * <p>
     * Clears the persistence context upon successful execution if rows were modified.
     *
     * @param fields     map of attribute names to their new values; must not be {@code null} or empty
     * @param conditions map of condition attribute names to their expected values; must not be {@code null} or empty
     * @return the total number of entities updated
     * @throws IllegalArgumentException if {@code fields} or {@code conditions} is {@code null} or empty
     */
    int updateFieldsByConditions(Map<String, Object> fields, Map<String, Object> conditions);

    // -------------------------------------------------------------
    // High-Performance Bulk CASE-WHEN Batch Updates
    // -------------------------------------------------------------

    /**
     * Performs a high-performance bulk update for uniform (even) attributes across multiple rows in a single query
     * using SQL/JPQL {@code CASE ... WHEN ... THEN} expressions.
     * <p>
     * Eliminates N-query bottlenecks by bundling all rows and attribute updates into one database operation.
     * Every row in {@code values} must provide a values list with length exactly matching {@code fields.size()}.
     * Clears the persistence context upon successful execution if rows were modified.
     *
     * @param fields ordered list of entity attribute names to update; must not be {@code null} or empty
     * @param values list of {@link IdValues} items, each containing an entity ID and matching values list
     * @return the number of entities updated, or {@code 0} if {@code values} is {@code null} or empty
     * @throws IllegalArgumentException if {@code fields} is {@code null}/empty, if any field name is blank,
     *                                  or if any {@link IdValues} item has a {@code null} ID or mismatched value count
     */
    int updateBulkEvenFields(List<String> fields, List<? extends IdValues> values);

    /**
     * Performs a high-performance bulk update for heterogeneous (uneven) attributes across multiple rows in a single query
     * using SQL/JPQL {@code CASE ... WHEN ... THEN ... ELSE column END} expressions.
     * <p>
     * Unlike {@link #updateBulkEvenFields}, each row in {@code dataList} may update a different subset of entity attributes.
     * Attributes omitted by a specific row retain their existing database values via the {@code ELSE} fallback clause.
     * Clears the persistence context upon successful execution if rows were modified.
     *
     * @param dataList list of {@link IdFieldValue} items, each containing an entity ID and a map of field-value pairs
     * @return the number of entities updated, or {@code 0} if {@code dataList} is {@code null} or empty
     * @throws IllegalArgumentException if any item in {@code dataList} is {@code null}, has a {@code null} ID,
     *                                  or has an empty/blank field key
     */
    int updateBulkUnevenFields(List<? extends IdFieldValue> dataList);

}
