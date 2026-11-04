package com.jiaul.jpahelper.service;

import com.jiaul.jpahelper.repository.BaseRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.Objects;

/**
 * Foundational abstract base class for all entity helper components.
 * <p>
 * Holds common references to the entity's runtime {@link Class}, its backing {@link BaseRepository},
 * and the JPA {@link EntityManager}. Upon Spring initialization, it resolves and caches the entity's
 * JPA {@link EntityType} metamodel, entity name, and primary key attribute name.
 *
 * @param <E>  the managed entity type
 * @param <ID> the primary key identifier type of the entity
 */
public abstract class Helper<E, ID> {

    /**
     * Backing Spring Data repository providing query execution and projection lookups.
     */
    protected final BaseRepository<E, ID> baseRepository;

    /**
     * Runtime {@link Class} of the managed entity.
     */
    protected final Class<E> entityClass;

    /**
     * Shared JPA {@link EntityManager} proxy injected into the persistence context.
     */
    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * JPA metamodel descriptor for entity {@code E}.
     */
    protected EntityType<E> entity;

    /**
     * JPA entity name (e.g., {@code "Student"}), resolved from {@link EntityType#getName()}.
     */
    protected String entityName;

    /**
     * Name of the primary key attribute (e.g., {@code "id"}), resolved from the entity metamodel.
     */
    protected String idName;

    /**
     * Constructs a new {@code Helper} instance for the specified entity class and repository.
     *
     * @param entityClass    the runtime class of the managed entity; must not be {@code null}
     * @param baseRepository the Spring Data repository backing the entity; must not be {@code null}
     * @throws NullPointerException if {@code entityClass} or {@code baseRepository} is {@code null}
     */
    protected Helper(Class<E> entityClass, BaseRepository<E, ID> baseRepository) {
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass must not be null");
        this.baseRepository = Objects.requireNonNull(baseRepository, "baseRepository must not be null");
    }

    /**
     * Post-construction callback that initializes entity metamodel metadata.
     * <p>
     * Resolves {@link #entity}, {@link #entityName}, and locates the singular {@code @Id} attribute to set
     * {@link #idName}.
     *
     * @throws IllegalArgumentException if the entity does not define an {@code @Id} attribute
     */
    @PostConstruct
    protected void initMetamodel() {
        this.entity = this.entityManager.getMetamodel().entity(entityClass);
        this.entityName = this.entity.getName();

        for (SingularAttribute<?, ?> attribute : this.entity.getSingularAttributes()) {
            if (attribute.isId()) {
                this.idName = attribute.getName();
                break;
            }
        }
        if (this.idName == null) {
            throw new IllegalArgumentException(this.entityName + " has no @Id attribute");
        }
    }
}
