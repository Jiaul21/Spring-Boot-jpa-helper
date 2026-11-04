package com.jiaul.jpahelper.service.base;

import com.jiaul.jpahelper.repository.BaseRepository;
import com.jiaul.jpahelper.service.Helper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BaseHelperImpl<E, ID> extends Helper<E, ID> implements BaseHelper<E, ID> {

    public BaseHelperImpl(Class<E> entityClass, BaseRepository<E, ID> baseRepository) {
        super(entityClass, baseRepository);
    }

    @Override
    public boolean existsById(ID id) {
        return baseRepository.existsById(id);
    }

    @Override
    public boolean existsByIdOrThrow(ID id) {
        if (!baseRepository.existsById(id)) {
            throw new EntityNotFoundException(entityName + " does not exist for " + idName + "=" + id);
        }
        return true;
    }

    @Override
    public <T> T findById(ID id, Class<T> type) {
        return baseRepository.findByID(id, type).orElse(null);
    }

    @Override
    public <T> T findByIdOrThrow(ID id, Class<T> type) {
        return baseRepository.findByID(id, type).orElseThrow(() ->
                new EntityNotFoundException(entityName + " not found for " + idName + "=" + id));
    }

    @Override
    public <T> List<T> findByIdIn(Collection<ID> ids, Class<T> type) {
        return (ids == null || ids.isEmpty()) ? new ArrayList<>() : baseRepository.findByIDsIn(ids, type);
    }

    @Override
    public List<ID> findExistingIds(Collection<ID> ids) {
        return (ids == null || ids.isEmpty()) ? new ArrayList<>() : baseRepository.findExistingIDs(ids);
    }

    @Override
    public E save(E entity) {
        return baseRepository.save(entity);
    }

    @Override
    public void delete(E entity) {
        baseRepository.delete(entity);
    }

    @Override
    public void deleteById(ID id) {
        baseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void transactionalProxy(Runnable action) {
        action.run();
    }
}
