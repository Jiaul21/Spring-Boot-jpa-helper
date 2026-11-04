package com.jiaul.jpahelper.service.updater;

import com.jiaul.jpahelper.dao.IdFieldValue;
import com.jiaul.jpahelper.dao.IdValues;
import com.jiaul.jpahelper.service.base.BaseHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface UpdateBaseHelper<E, ID> extends BaseHelper<E, ID> {

    E updateEntity(ID id, Consumer<E> updater);

    int updateFieldByID(ID id, String fieldName, Object value);

    int updateFieldsByIDIn(Collection<ID> ids, Map<String, Object> fields);

    int updateField(String fieldName, Object fieldValue, String cName, Object cValue);

    int updateFieldsByConditions(Map<String, Object> fields, Map<String, Object> conditions);

    int updateBulkEvenFields(List<String> fields, List<? extends IdValues> values);

    int updateBulkUnevenFields(List<? extends IdFieldValue> dataList);

}
