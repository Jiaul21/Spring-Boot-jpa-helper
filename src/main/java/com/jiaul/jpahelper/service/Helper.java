package com.jiaul.jpahelper.service;

import com.jiaul.jpahelper.repository.BaseRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.Objects;

public abstract class Helper<E, ID> {

    protected final BaseRepository<E, ID> baseRepository;
    protected final Class<E> entityClass;

    @PersistenceContext
    protected EntityManager entityManager;

    protected EntityType<E> entity;
    protected String entityName;
    protected String idName;

    protected Helper(Class<E> entityClass, BaseRepository<E, ID> baseRepository) {
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass must not be null");
        this.baseRepository = Objects.requireNonNull(baseRepository, "baseRepository must not be null");
    }

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
