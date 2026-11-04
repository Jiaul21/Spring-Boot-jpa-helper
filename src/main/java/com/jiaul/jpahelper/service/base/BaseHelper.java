package com.jiaul.jpahelper.service.base;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface BaseHelper<E, ID> {

    boolean existsById(ID id);

    boolean existsByIdOrThrow(ID id);

    <T> T findById(ID id, Class<T> type);

    <T> T findByIdOrThrow(ID id, Class<T> type);

    <T> List<T> findByIdIn(Collection<ID> ids, Class<T> type);

    List<ID> findExistingIds(Collection<ID> ids);

    default <C extends Collection<ID>> C findExistingIds(Collection<ID> ids, Function<? super List<ID>, ? extends C> copier) {
        return copier.apply(findExistingIds(ids));
    }

    E save(E entity);

    void delete(E entity);

    void deleteById(ID id);

    void transactionalProxy(Runnable action);
}
