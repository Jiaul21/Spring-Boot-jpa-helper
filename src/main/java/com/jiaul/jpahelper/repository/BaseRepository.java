package com.jiaul.jpahelper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Base Spring Data JPA repository providing dynamic projection and ID-existence queries.
 * <p>
 * Marked with {@link NoRepositoryBean} so Spring Data does not attempt to instantiate it directly.
 * All domain entity repositories in consumer applications should extend this interface:
 * <pre>{@code
 * public interface StudentRepository extends BaseRepository<Student, Long> {
 *     // custom repository queries
 * }
 * }</pre>
 * <p>
 * Leverages Spring Data SpEL expressions ({@code #{#entityName}}) and Hibernate's polymorphic
 * {@code id(e)} function to work seamlessly across any entity type without repetitive JPQL queries.
 *
 * @param <E>  the managed domain entity type
 * @param <ID> the primary key identifier type of the entity
 */
@NoRepositoryBean
public interface BaseRepository<E, ID> extends JpaRepository<E, ID> {

    /**
     * Retrieves an entity by its identifier dynamically projected into the requested target type.
     * <p>
     * Supports Spring Data interface-based projections, DTO classes, Java records, or the entity itself.
     *
     * @param <T>  the projection target type
     * @param id   the entity identifier
     * @param type the target class to project the entity into
     * @return an {@link Optional} containing the projected result, or {@link Optional#empty()} if not found
     */
    @Query("select e from #{#entityName} e where id(e) = :id")
    <T> Optional<T> findByID(@Param("id") ID id, Class<T> type);

    /**
     * Retrieves all entities matching the specified identifiers dynamically projected into the requested target type.
     *
     * @param <T>  the projection target type
     * @param ids  collection of entity identifiers to retrieve
     * @param type the target class to project entities into
     * @return list of projected results matching the given identifiers; never {@code null}
     */
    @Query("select e from #{#entityName} e where id(e) in :ids")
    <T> List<T> findByIDsIn(@Param("ids") Collection<ID> ids, Class<T> type);

    /**
     * Queries and returns only the subset of identifiers that actually exist in the database.
     * <p>
     * Executes a lightweight {@code SELECT id(e)} query without fetching entity associations or table columns,
     * ideal for validating batch IDs or checking references before bulk operations.
     *
     * @param ids collection of candidate entity identifiers to verify
     * @return list of identifiers that exist in the database; empty list if none exist
     */
    @Query("select id(e) from #{#entityName} e where id(e) in :ids")
    List<ID> findExistingIDs(@Param("ids") Collection<ID> ids);

}
