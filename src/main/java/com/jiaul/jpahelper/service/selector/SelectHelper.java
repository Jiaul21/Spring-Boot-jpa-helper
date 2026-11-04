package com.jiaul.jpahelper.service.selector;

import com.jiaul.jpahelper.service.updater.UpdateHelper;
import jakarta.persistence.TypedQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service helper interface providing dynamic multi-level relation fetch queries,
 * condition-based lookups, and query construction utilities.
 * <p>
 * Key capabilities:
 * <ul>
 *   <li><b>Dynamic Fetch Joins:</b> Supply dotted relation paths (e.g., {@code "department"}, {@code "profile.advisor"},
 *       {@code "courses.teacher"}) to eagerly initialize associations via {@code LEFT JOIN FETCH} without LazyInitializationException.</li>
 *   <li><b>Cartesian Product Protection:</b> Automatically analyzes requested relations against the JPA Metamodel to prevent
 *       joining multiple parallel collections simultaneously, avoiding Hibernate {@code MultipleBagFetchException} and row explosion.</li>
 *   <li><b>Automatic Condition Joins:</b> When filtering by nested properties (e.g., {@code "department.code"}), the necessary
 *       joins are automatically generated even if omitted from {@code joinFields}.</li>
 *   <li><b>Metamodel Caching:</b> Validates and caches association paths using concurrent thread-safe sets for near-zero runtime overhead.</li>
 * </ul>
 *
 * @param <E>  the managed domain entity type
 * @param <ID> the primary key identifier type of the entity
 */
public interface SelectHelper<E, ID> extends UpdateHelper<E, ID> {

    // -------------------------------------------------------------
    // Query Builders (TypedQuery)
    // -------------------------------------------------------------

    /**
     * Builds a {@link TypedQuery} filtered by a single condition with optional eager fetch joins.
     * <p>
     * Enables callers to customize query hints (e.g., query timeouts, cache modes) or configure pagination
     * before executing.
     *
     * @param cName      the condition property name or nested path (e.g., {@code "email"} or {@code "department.name"}); must not be {@code null} or blank
     * @param cValue     the expected condition value (or {@code null} to match {@code IS NULL})
     * @param joinFields list of relation attribute paths to eagerly fetch; can be {@code null} or empty
     * @return the configured {@link TypedQuery}
     * @throws IllegalArgumentException if {@code cName} is {@code null} or blank
     */
    TypedQuery<E> buildFetchQuery(String cName, Object cValue, List<String> joinFields);

    /**
     * Builds a {@link TypedQuery} filtered by a single condition without additional fetch joins.
     *
     * @param cName  the condition property name or nested path; must not be {@code null} or blank
     * @param cValue the expected condition value
     * @return the configured {@link TypedQuery}
     * @throws IllegalArgumentException if {@code cName} is {@code null} or blank
     */
    default TypedQuery<E> buildFetchQuery(String cName, Object cValue) {
        return buildFetchQuery(cName, cValue, Collections.emptyList());
    }

    /**
     * Builds a {@link TypedQuery} filtered by multiple {@code AND} conditions with optional eager fetch joins.
     *
     * @param conditions map of condition attribute paths to their expected values; must not be {@code null} or empty
     * @param joinFields list of relation attribute paths to eagerly fetch; can be {@code null} or empty
     * @return the configured {@link TypedQuery}
     * @throws IllegalArgumentException if {@code conditions} is {@code null} or empty
     */
    TypedQuery<E> buildFetchQuery(Map<String, Object> conditions, List<String> joinFields);

    /**
     * Builds a {@link TypedQuery} filtered by multiple {@code AND} conditions without additional fetch joins.
     *
     * @param conditions map of condition attribute paths to their expected values; must not be {@code null} or empty
     * @return the configured {@link TypedQuery}
     * @throws IllegalArgumentException if {@code conditions} is {@code null} or empty
     */
    default TypedQuery<E> buildFetchQuery(Map<String, Object> conditions) {
        return buildFetchQuery(conditions, Collections.emptyList());
    }

    /**
     * Builds a {@link TypedQuery} matching a collection of entity identifiers ({@code WHERE id(e) IN :ids})
     * with optional eager fetch joins.
     *
     * @param ids        collection of entity identifiers to retrieve; must not be {@code null} or empty
     * @param joinFields list of relation attribute paths to eagerly fetch; can be {@code null} or empty
     * @return the configured {@link TypedQuery}
     * @throws IllegalArgumentException if {@code ids} is {@code null} or empty
     */
    TypedQuery<E> buildFetchByIDInQuery(Collection<ID> ids, List<String> joinFields);

    /**
     * Builds a {@link TypedQuery} matching a collection of entity identifiers without additional fetch joins.
     *
     * @param ids collection of entity identifiers to retrieve; must not be {@code null} or empty
     * @return the configured {@link TypedQuery}
     * @throws IllegalArgumentException if {@code ids} is {@code null} or empty
     */
    default TypedQuery<E> buildFetchByIDInQuery(Collection<ID> ids) {
        return buildFetchByIDInQuery(ids, Collections.emptyList());
    }

    // -------------------------------------------------------------
    // Single Entity Retrieval
    // -------------------------------------------------------------

    /**
     * Retrieves an entity by its primary key with specified relations eagerly initialized.
     * <p>
     * If {@code joinFields} is empty, delegates directly to {@link jakarta.persistence.EntityManager#find} to benefit
     * from first-level session caching without running an SQL query if the entity is already loaded.
     *
     * @param id         the entity identifier; must not be {@code null}
     * @param joinFields list of relation attribute paths to eagerly fetch
     * @return the entity matching {@code id}, or {@code null} if not found
     * @throws NullPointerException if {@code id} is {@code null}
     */
    E fetchByID(ID id, List<String> joinFields);

    /**
     * Retrieves an entity by its primary key using first-level persistence context lookup.
     *
     * @param id the entity identifier; must not be {@code null}
     * @return the entity matching {@code id}, or {@code null} if not found
     * @throws NullPointerException if {@code id} is {@code null}
     */
    default E fetchByID(ID id) {
        return fetchByID(id, Collections.emptyList());
    }

    /**
     * Retrieves the first entity matching the specified condition with optional eager fetch joins.
     *
     * @param cName      the condition property name or nested path
     * @param cValue     the expected condition value
     * @param joinFields list of relation attribute paths to eagerly fetch
     * @return the first matching entity, or {@code null} if no match is found
     */
    default E fetchOne(String cName, Object cValue, List<String> joinFields) {
        return buildFetchQuery(cName, cValue, joinFields).getResultStream().findFirst().orElse(null);
    }

    /**
     * Retrieves the first entity matching the specified condition without additional fetch joins.
     *
     * @param cName  the condition property name or nested path
     * @param cValue the expected condition value
     * @return the first matching entity, or {@code null} if no match is found
     */
    default E fetchOne(String cName, Object cValue) {
        return fetchOne(cName, cValue, Collections.emptyList());
    }

    /**
     * Retrieves the first entity matching multiple {@code AND} conditions with optional eager fetch joins.
     *
     * @param conditions map of condition attribute paths to their expected values
     * @param joinFields list of relation attribute paths to eagerly fetch
     * @return the first matching entity, or {@code null} if no match is found
     */
    default E fetchOne(Map<String, Object> conditions, List<String> joinFields) {
        return buildFetchQuery(conditions, joinFields).getResultStream().findFirst().orElse(null);
    }

    /**
     * Retrieves the first entity matching multiple {@code AND} conditions without additional fetch joins.
     *
     * @param conditions map of condition attribute paths to their expected values
     * @return the first matching entity, or {@code null} if no match is found
     */
    default E fetchOne(Map<String, Object> conditions) {
        return fetchOne(conditions, Collections.emptyList());
    }

    // -------------------------------------------------------------
    // List Entity Retrieval
    // -------------------------------------------------------------

    /**
     * Retrieves all entities matching a single condition with specified relations eagerly initialized.
     *
     * @param cName      the condition property name or nested path; must not be {@code null} or blank
     * @param cValue     the expected condition value
     * @param joinFields list of relation attribute paths to eagerly fetch
     * @return list of matching entities; never {@code null}
     */
    List<E> fetchBy(String cName, Object cValue, List<String> joinFields);

    /**
     * Retrieves all entities matching a single condition without additional fetch joins.
     *
     * @param cName  the condition property name or nested path; must not be {@code null} or blank
     * @param cValue the expected condition value
     * @return list of matching entities; never {@code null}
     */
    default List<E> fetchBy(String cName, Object cValue) {
        return fetchBy(cName, cValue, Collections.emptyList());
    }

    /**
     * Retrieves all entities matching multiple {@code AND} conditions with specified relations eagerly initialized.
     *
     * @param conditions map of condition attribute paths to their expected values; must not be {@code null} or empty
     * @param joinFields list of relation attribute paths to eagerly fetch
     * @return list of matching entities; never {@code null}
     */
    List<E> fetchBy(Map<String, Object> conditions, List<String> joinFields);

    /**
     * Retrieves all entities matching multiple {@code AND} conditions without additional fetch joins.
     *
     * @param conditions map of condition attribute paths to their expected values; must not be {@code null} or empty
     * @return list of matching entities; never {@code null}
     */
    default List<E> fetchBy(Map<String, Object> conditions) {
        return fetchBy(conditions, Collections.emptyList());
    }

    /**
     * Retrieves all entities matching a collection of identifiers ({@code WHERE id(e) IN :ids})
     * with specified relations eagerly initialized.
     *
     * @param ids        collection of entity identifiers to retrieve
     * @param joinFields list of relation attribute paths to eagerly fetch
     * @return list of matching entities; empty list if {@code ids} is {@code null} or empty
     */
    List<E> fetchByIDIn(Collection<ID> ids, List<String> joinFields);

    /**
     * Retrieves all entities matching a collection of identifiers without additional fetch joins.
     *
     * @param ids collection of entity identifiers to retrieve
     * @return list of matching entities; empty list if {@code ids} is {@code null} or empty
     */
    default List<E> fetchByIDIn(Collection<ID> ids) {
        return fetchByIDIn(ids, Collections.emptyList());
    }

    // -------------------------------------------------------------
    // Backward-Compatibility Aliases
    // -------------------------------------------------------------

    /**
     * @deprecated Use {@link #fetchByID(Object, List)} instead.
     */
    @Deprecated
    default E fetchNestedJoin(ID id, List<String> joinFields) {
        return fetchByID(id, joinFields);
    }

    /**
     * @deprecated Use {@link #fetchBy(String, Object, List)} instead.
     */
    @Deprecated
    default List<E> fetchNestedJoin(String cName, Object cValue, List<String> joinFields) {
        return fetchBy(cName, cValue, joinFields);
    }

    /**
     * @deprecated Use {@link #fetchBy(Map, List)} instead.
     */
    @Deprecated
    default List<E> fetchNestedJoin(Map<String, Object> conditions, List<String> joinFields) {
        return fetchBy(conditions, joinFields);
    }
}
