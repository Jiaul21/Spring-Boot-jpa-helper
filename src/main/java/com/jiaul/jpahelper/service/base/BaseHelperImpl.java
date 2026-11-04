package com.jiaul.jpahelper.service.base;

import com.jiaul.jpahelper.repository.BaseRepository;
import com.jiaul.jpahelper.service.Helper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Standard implementation of {@link BaseHelper} providing foundational repository-backed operations.
 * <p>
 * Extends {@link Helper} to leverage cached entity metamodel attributes (e.g. {@code entityName}, {@code idName})
 * when composing detailed diagnostic error messages.
 *
 * @param <E>  the managed domain entity type
 * @param <ID> the primary key identifier type of the entity
 */
public class BaseHelperImpl<E, ID> extends Helper<E, ID> implements BaseHelper<E, ID> {

    /**
     * Constructs a new {@code BaseHelperImpl} backed by the given entity class and repository.
     *
     * @param entityClass    the runtime class of the managed entity; must not be {@code null}
     * @param baseRepository the Spring Data repository backing the entity; must not be {@code null}
     * @throws NullPointerException if {@code entityClass} or {@code baseRepository} is {@code null}
     */
    public BaseHelperImpl(Class<E> entityClass, BaseRepository<E, ID> baseRepository) {
        super(entityClass, baseRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsById(ID id) {
        return baseRepository.existsById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @throws EntityNotFoundException if no entity is found for {@code id}
     */
    @Override
    public boolean existsByIdOrThrow(ID id) {
        if (!baseRepository.existsById(id)) {
            throw new EntityNotFoundException(entityName + " does not exist for " + idName + "=" + id);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T findById(ID id, Class<T> type) {
        return baseRepository.findByID(id, type).orElse(null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws EntityNotFoundException if no entity is found for {@code id}
     */
    @Override
    public <T> T findByIdOrThrow(ID id, Class<T> type) {
        return baseRepository.findByID(id, type).orElseThrow(() ->
                new EntityNotFoundException(entityName + " not found for " + idName + "=" + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> findByIdIn(Collection<ID> ids, Class<T> type) {
        return (ids == null || ids.isEmpty()) ? new ArrayList<>() : baseRepository.findByIDsIn(ids, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ID> findExistingIds(Collection<ID> ids) {
        return (ids == null || ids.isEmpty()) ? new ArrayList<>() : baseRepository.findExistingIDs(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E save(E entity) {
        return baseRepository.save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(E entity) {
        baseRepository.delete(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(ID id) {
        baseRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Annotated with {@link Transactional} to ensure execution within an active Spring transaction.
     */
    @Override
    @Transactional
    public void transactionalProxy(Runnable action) {
        action.run();
    }
}
