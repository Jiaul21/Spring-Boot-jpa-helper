package com.jiaul.jpahelper.service.updater;

import com.jiaul.jpahelper.dao.IdFieldValue;
import com.jiaul.jpahelper.dao.IdValues;
import com.jiaul.jpahelper.repository.BaseRepository;
import com.jiaul.jpahelper.service.base.BaseHelperImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;

public class UpdateBaseHelperImpl<E, ID> extends BaseHelperImpl<E, ID> implements UpdateBaseHelper<E, ID> {

    public UpdateBaseHelperImpl(Class<E> entityClass, BaseRepository<E, ID> baseRepository) {
        super(entityClass, baseRepository);
    }

    @Override
    @Transactional
    public E updateEntity(ID id, Consumer<E> updater) {
        Objects.requireNonNull(id, "ID must not be null.");
        Objects.requireNonNull(updater, "Updater object must not be null.");

        E entity = entityManager.find(entityClass, id);
        if (entity == null) {
            throw new EntityNotFoundException(entityName + " entity reference not found for " + idName + "=" + id);
        }
        updater.accept(entity);
        return entity;
    }

    @Override
    @Transactional
    public int updateFieldByID(ID id, String fieldName, Object value) {
        if (id == null || fieldName == null || fieldName.isBlank()) {
            throw new IllegalArgumentException(
                    "ID and fieldName must not be null or blank. ID=" + id + ", fieldName=" + fieldName);
        }
        String jpql = "UPDATE " + entityName + " e SET e." + fieldName + " = :value WHERE id(e) = :ID";
        int updated = entityManager.createQuery(jpql)
                .setParameter("value", value)
                .setParameter("ID", id)
                .executeUpdate();

        if (updated > 0) {
            entityManager.clear();
        }
        return updated;
    }

    @Override
    @Transactional
    public int updateFieldsByIDIn(Collection<ID> ids, Map<String, Object> fields) {
        if (ids == null || ids.isEmpty() || fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException(
                    "Update query failed because of IDs or fields is null or empty. IDs=" + ids + ", fields=" + fields);
        }
        StringBuilder jpql = new StringBuilder("UPDATE ").append(entityName).append(" e SET ");

        int i = 0;
        for (String field : fields.keySet()) {
            if (i > 0) {
                jpql.append(", ");
            }
            jpql.append("e.").append(field).append(" = :v_").append(i++);
        }
        jpql.append(" WHERE id(e) IN :IDs");

        Query query = entityManager.createQuery(jpql.toString());
        query.setParameter("IDs", ids);

        int idx = 0;
        for (Object value : fields.values()) {
            query.setParameter("v_" + idx++, value);
        }

        int updated = query.executeUpdate();
        if (updated > 0) {
            entityManager.clear();
        }
        return updated;
    }

    @Override
    @Transactional
    public int updateField(String fieldName, Object fieldValue, String cName, Object cValue) {
        if (fieldName == null || fieldName.isBlank() || cName == null || cName.isBlank()) {
            throw new IllegalArgumentException(
                    "fieldName and cName must not be null or blank. fieldName=" + fieldName + ", cName=" + cName);
        }

        String jpql = "UPDATE " + entityName + " e SET e." + fieldName + " = :value WHERE e." + cName
                + (cValue == null ? " IS NULL" : " = :c");

        Query query = entityManager.createQuery(jpql).setParameter("value", fieldValue);
        if (cValue != null) {
            query.setParameter("c", cValue);
        }

        int updated = query.executeUpdate();
        if (updated > 0) {
            entityManager.clear();
        }
        return updated;
    }

    @Override
    @Transactional
    public int updateFieldsByConditions(Map<String, Object> fields, Map<String, Object> conditions) {
        if (fields == null || fields.isEmpty() || conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Fields and conditions must not be null or empty. fields=" + fields + ", conditions=" + conditions);
        }
        StringBuilder jpql = new StringBuilder("UPDATE ").append(entityName).append(" e SET ");

        int i = 0;
        for (String field : fields.keySet()) {
            if (i > 0) {
                jpql.append(", ");
            }
            jpql.append("e.").append(field).append(" = :set_").append(i++);
        }
        jpql.append(" WHERE ");

        int j = 0;
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            if (j > 0) {
                jpql.append(" AND ");
            }
            if (entry.getValue() == null) {
                jpql.append("e.").append(entry.getKey()).append(" IS NULL");
            } else {
                jpql.append("e.").append(entry.getKey()).append(" = :cond_").append(j);
            }
            j++;
        }

        Query query = entityManager.createQuery(jpql.toString());

        int setIdx = 0;
        for (Object value : fields.values()) {
            query.setParameter("set_" + setIdx++, value);
        }

        int condIdx = 0;
        for (Object value : conditions.values()) {
            if (value != null) {
                query.setParameter("cond_" + condIdx, value);
            }
            condIdx++;
        }

        int updated = query.executeUpdate();
        if (updated > 0) {
            entityManager.clear();
        }
        return updated;
    }

    @Override
    @Transactional
    public int updateBulkEvenFields(List<String> fields, List<? extends IdValues> values) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("fields must not be null or empty. fields=" + fields);
        }
        if (values == null || values.isEmpty()) {
            return 0;
        }

        int fieldCount = fields.size();
        StringBuilder[] caseBuilders = new StringBuilder[fieldCount];
        for (int f = 0; f < fieldCount; f++) {
            String field = fields.get(f);
            if (field == null || field.isBlank()) {
                throw new IllegalArgumentException(
                        "Field name must not be null or blank at index " + f + ". fields=" + fields);
            }
            caseBuilders[f] = new StringBuilder("e.").append(field).append(" = CASE id(e)");
        }

        List<Object> ids = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            IdValues item = values.get(i);
            if (item == null || item.getID() == null) {
                throw new IllegalArgumentException("Each IdValues item and its ID must not be null at index " + i);
            }
            List<Object> rowValues = item.getValues();
            if (rowValues == null || rowValues.size() != fieldCount) {
                throw new IllegalArgumentException("Values count must match fields count (" + fieldCount + ") for ID="
                        + item.getID() + ", but got: " + (rowValues == null ? "null" : rowValues.size()));
            }
            ids.add(item.getID());
            for (int f = 0; f < fieldCount; f++) {
                caseBuilders[f].append(" WHEN :id_").append(i).append(" THEN :v_").append(f).append('_').append(i);
            }
        }

        StringBuilder jpql = new StringBuilder("UPDATE ").append(entityName).append(" e SET ");
        for (int f = 0; f < fieldCount; f++) {
            if (f > 0) {
                jpql.append(", ");
            }
            jpql.append(caseBuilders[f]).append(" END");
        }
        jpql.append(" WHERE id(e) IN :IDs");

        Query query = entityManager.createQuery(jpql.toString());
        for (int i = 0; i < values.size(); i++) {
            IdValues item = values.get(i);
            query.setParameter("id_" + i, item.getID());
            List<Object> rowValues = item.getValues();
            for (int f = 0; f < fieldCount; f++) {
                query.setParameter("v_" + f + "_" + i, rowValues.get(f));
            }
        }
        query.setParameter("IDs", ids);

        int updated = query.executeUpdate();
        if (updated > 0) {
            entityManager.clear();
        }
        return updated;
    }

    @Override
    @Transactional
    public int updateBulkUnevenFields(List<? extends IdFieldValue> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        Map<String, StringBuilder> caseBuilders = new LinkedHashMap<>();
        List<Object> ids = new ArrayList<>(dataList.size());

        for (int i = 0; i < dataList.size(); i++) {
            IdFieldValue item = dataList.get(i);
            if (item == null || item.getID() == null) {
                throw new IllegalArgumentException("Each IdFieldValue item and its ID must not be null at index " + i);
            }
            Map<String, Object> fieldMap = item.getFieldValue();
            if (fieldMap == null || fieldMap.isEmpty()) {
                throw new IllegalArgumentException("fieldValue map must not be null or empty for ID=" + item.getID());
            }
            ids.add(item.getID());

            int entryIdx = 0;
            for (String field : fieldMap.keySet()) {
                if (field == null || field.isBlank()) {
                    throw new IllegalArgumentException("Field name must not be null or blank for ID=" + item.getID());
                }
                caseBuilders.computeIfAbsent(field, f -> new StringBuilder("e.").append(f).append(" = CASE id(e)"))
                        .append(" WHEN :id_").append(i).append(" THEN :v_").append(i).append('_').append(entryIdx++);
            }
        }

        StringBuilder jpql = new StringBuilder("UPDATE ").append(entityName).append(" e SET ");
        int fIdx = 0;
        for (Map.Entry<String, StringBuilder> entry : caseBuilders.entrySet()) {
            if (fIdx++ > 0) {
                jpql.append(", ");
            }
            jpql.append(entry.getValue()).append(" ELSE e.").append(entry.getKey()).append(" END");
        }
        jpql.append(" WHERE id(e) IN :IDs");

        Query query = entityManager.createQuery(jpql.toString());
        for (int i = 0; i < dataList.size(); i++) {
            IdFieldValue item = dataList.get(i);
            query.setParameter("id_" + i, item.getID());
            int entryIdx = 0;
            for (Object value : item.getFieldValue().values()) {
                query.setParameter("v_" + i + "_" + entryIdx++, value);
            }
        }
        query.setParameter("IDs", ids);

        int updated = query.executeUpdate();
        if (updated > 0) {
            entityManager.clear();
        }
        return updated;
    }
}
