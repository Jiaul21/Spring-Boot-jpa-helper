package com.jiaul.jpahelper.service.base;

import jakarta.persistence.EntityNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Foundation helper interface providing standard CRUD, existence checks, projection lookups, and transactional utilities.
 * <p>
 * Forms the base layer of the entity helper suite, delegating core persistence operations to the underlying
 * {@link com.jiaul.jpahelper.repository.BaseRepository}.
 *
 * @param <E>  the managed domain entity type
 * @param <ID> the primary key identifier type of the entity
 */
public interface BaseHelper<E, ID> {

    // -------------------------------------------------------------
    // Existence Checks
    // -------------------------------------------------------------

    /**
     * Checks whether an entity with the specified identifier exists in the database.
     *
     * @param id the entity identifier to check
     * @return {@code true} if an entity with the given ID exists, {@code false} otherwise
     */
    boolean existsById(ID id);

    /**
     * Verifies that an entity with the specified identifier exists, or throws an exception.
     *
     * @param id the entity identifier to verify
     * @return {@code true} if the entity exists
     * @throws EntityNotFoundException if no entity exists for the given ID
     */
    boolean existsByIdOrThrow(ID id);

    // -------------------------------------------------------------
    // Projection Lookups
    // -------------------------------------------------------------

    /**
     * Finds an entity by its identifier and projects the result into the requested target type.
     * <p>
     * Supports Spring Data interface-based projections, DTO classes, Java records, or the entity type itself.
     *
     * @param <T>  the projection target type
     * @param id   the entity identifier
     * @param type the target class to project the entity into
     * @return the projected instance, or {@code null} if not found
     */
    <T> T findById(ID id, Class<T> type);

    /**
     * Finds an entity by its identifier projected into the requested target type, throwing an exception if not found.
     *
     * @param <T>  the projection target type
     * @param id   the entity identifier
     * @param type the target class to project the entity into
     * @return the projected instance; never {@code null}
     * @throws EntityNotFoundException if no entity exists for the given ID
     */
    <T> T findByIdOrThrow(ID id, Class<T> type);

    /**
     * Finds multiple entities matching the specified collection of identifiers projected into the requested target type.
     *
     * @param <T>  the projection target type
     * @param ids  the collection of identifiers to retrieve
     * @param type the target class to project entities into
     * @return list of projected results; empty list if {@code ids} is {@code null} or empty
     */
    <T> List<T> findByIdIn(Collection<ID> ids, Class<T> type);

    // -------------------------------------------------------------
    // ID Existence Verification
    // -------------------------------------------------------------

    /**
     * Queries and returns only the subset of identifiers that actually exist in the database.
     * <p>
     * Executes a lightweight {@code SELECT id(e)} query without loading entire entities into memory.
     *
     * @param ids collection of candidate identifiers to check
     * @return list containing only the identifiers that exist; empty list if {@code ids} is {@code null} or empty
     */
    List<ID> findExistingIds(Collection<ID> ids);

    /**
     * Queries existing identifiers and maps them into a custom collection type via the provided factory function.
     * <p>
     * For example, converting existing IDs directly into a {@link java.util.Set}:
     * <pre>{@code
     * Set<Long> existing = helper.findExistingIds(candidateIds, HashSet::new);
     * }</pre>
     *
     * @param <C>    the target collection type
     * @param ids    collection of candidate identifiers to check
     * @param copier factory function to transform the list of found IDs into collection {@code C}
     * @return the populated collection containing existing identifiers
     */
    default <C extends Collection<ID>> C findExistingIds(Collection<ID> ids, Function<? super List<ID>, ? extends C> copier) {
        return copier.apply(findExistingIds(ids));
    }

    // -------------------------------------------------------------
    // Persistence Operations
    // -------------------------------------------------------------

    /**
     * Persists a new entity or merges changes of an existing entity.
     *
     * @param entity the entity instance to save
     * @return the saved entity instance returned by the repository
     */
    E save(E entity);

    /**
     * Deletes the specified entity from the database.
     *
     * @param entity the entity instance to delete
     */
    void delete(E entity);

    /**
     * Deletes the entity with the specified identifier.
     *
     * @param id the identifier of the entity to delete
     */
    void deleteById(ID id);

    // -------------------------------------------------------------
    // Transactional Utilities
    // -------------------------------------------------------------

    /**
     * Executes the given action within an active transactional boundary.
     * <p>
     * Useful for running arbitrary code blocks inside a managed transaction without requiring
     * {@code @Transactional} on the caller method.
     *
     * @param action the programmatic action to execute within a transaction
     */
    void transactionalProxy(Runnable action);
}
