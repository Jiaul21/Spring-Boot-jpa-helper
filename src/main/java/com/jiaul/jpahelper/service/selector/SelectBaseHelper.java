package com.jiaul.jpahelper.service.selector;

import com.jiaul.jpahelper.service.updater.UpdateBaseHelper;
import jakarta.persistence.TypedQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface SelectBaseHelper<E, ID> extends UpdateBaseHelper<E, ID> {

    TypedQuery<E> buildFetchQuery(String cName, Object cValue, List<String> joinFields);

    TypedQuery<E> buildFetchQuery(Map<String, Object> conditions, List<String> joinFields);


    E fetchByID(ID id, List<String> joinFields);

    List<E> fetchBy(String cName, Object cValue, List<String> joinFields);

    List<E> fetchBy(Map<String, Object> conditions, List<String> joinFields);

    List<E> fetchByIDIn(Collection<ID> ids, List<String> joinFields);


    default List<E> fetchBy(String cName, Object cValue) {
        return fetchBy(cName, cValue, Collections.emptyList());
    }

    default List<E> fetchBy(Map<String, Object> conditions) {
        return fetchBy(conditions, Collections.emptyList());
    }

    default E fetchOne(String cName, Object cValue, List<String> joinFields) {
        return buildFetchQuery(cName, cValue, joinFields).getResultStream().findFirst().orElse(null);
    }

    default E fetchOne(String cName, Object cValue) {
        return fetchOne(cName, cValue, Collections.emptyList());
    }

    default E fetchOne(Map<String, Object> conditions, List<String> joinFields) {
        return buildFetchQuery(conditions, joinFields).getResultStream().findFirst().orElse(null);
    }

    default E fetchOne(Map<String, Object> conditions) {
        return fetchOne(conditions, Collections.emptyList());
    }

}
