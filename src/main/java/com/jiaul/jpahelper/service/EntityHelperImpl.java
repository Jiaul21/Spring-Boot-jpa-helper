package com.jiaul.jpahelper.service;

import com.jiaul.jpahelper.repository.BaseRepository;
import com.jiaul.jpahelper.service.selector.SelectBaseHelperImpl;

public class EntityHelperImpl<E, ID> extends SelectBaseHelperImpl<E, ID> implements EntityHelper<E, ID> {

    public EntityHelperImpl(Class<E> entityClass, BaseRepository<E, ID> baseRepository) {
        super(entityClass, baseRepository);
    }
}
