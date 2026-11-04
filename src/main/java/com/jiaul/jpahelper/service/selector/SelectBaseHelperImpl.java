package com.jiaul.jpahelper.service.selector;

import com.jiaul.jpahelper.repository.BaseRepository;
import com.jiaul.jpahelper.service.updater.UpdateBaseHelperImpl;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SelectBaseHelperImpl<E, ID> extends UpdateBaseHelperImpl<E, ID> implements SelectBaseHelper<E, ID> {

    protected final Set<String> singleRelations = ConcurrentHashMap.newKeySet();
    protected final Set<String> collectionRelations = ConcurrentHashMap.newKeySet();

    public SelectBaseHelperImpl(Class<E> entityClass, BaseRepository<E, ID> baseRepository) {
        super(entityClass, baseRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public E fetchByID(ID id, List<String> joinFields) {
        Objects.requireNonNull(id, "ID must not be null.");
        if (joinFields == null || joinFields.isEmpty()) {
            return entityManager.find(entityClass, id);
        }
        return buildFetchQuery(idName, id, joinFields).getResultStream().findFirst().orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> fetchBy(String cName, Object cValue, List<String> joinFields) {
        return buildFetchQuery(cName, cValue, joinFields).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> fetchByIDIn(Collection<ID> ids, List<String> joinFields) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return buildFetchByIDInQuery(ids, joinFields).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> fetchBy(Map<String, Object> conditions, List<String> joinFields) {
        return buildFetchQuery(conditions, joinFields).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public TypedQuery<E> buildFetchQuery(String cName, Object cValue, List<String> joinFields) {
        if (cName == null || cName.isBlank()) {
            throw new IllegalArgumentException("Condition field name must not be null or blank.");
        }

        Map<String, String> mainAliases = new HashMap<>();
        StringBuilder mainQuery = initBaseFetchQuery(joinFields, mainAliases);

        // Build single condition directly - zero Map allocation
        String conditionClause = buildConditionClause(mainQuery, cName, cValue, "cVal", mainAliases);
        mainQuery.append(" WHERE ").append(conditionClause);

        TypedQuery<E> query = entityManager.createQuery(mainQuery.toString(), entityClass);
        if (cValue != null) {
            query.setParameter("cVal", cValue);
        }

        return query;
    }

    @Override
    @Transactional(readOnly = true)
    public TypedQuery<E> buildFetchQuery(Map<String, Object> conditions, List<String> joinFields) {
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("Conditions map must not be null or empty.");
        }

        Map<String, String> mainAliases = new HashMap<>();
        StringBuilder mainQuery = initBaseFetchQuery(joinFields, mainAliases);

        StringBuilder whereClause = new StringBuilder();
        int idx = 0;
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String cName = entry.getKey();
            if (cName == null || cName.isBlank()) {
                throw new IllegalArgumentException("Condition field name must not be null or blank.");
            }
            if (idx > 0) {
                whereClause.append(" AND ");
            }
            String paramName = "cond_" + idx;
            whereClause.append(buildConditionClause(mainQuery, cName, entry.getValue(), paramName, mainAliases));
            idx++;
        }
        mainQuery.append(" WHERE ").append(whereClause);

        TypedQuery<E> query = entityManager.createQuery(mainQuery.toString(), entityClass);
        int paramIdx = 0;
        for (Object value : conditions.values()) {
            if (value != null) {
                query.setParameter("cond_" + paramIdx, value);
            }
            paramIdx++;
        }

        return query;
    }

    @Transactional(readOnly = true)
    public TypedQuery<E> buildFetchByIDInQuery(Collection<ID> ids, List<String> joinFields) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("ids collection must not be null or empty.");
        }

        Map<String, String> mainAliases = new HashMap<>();
        StringBuilder mainQuery = initBaseFetchQuery(joinFields, mainAliases);
        mainQuery.append(" WHERE id(e) IN :ids");

        TypedQuery<E> query = entityManager.createQuery(mainQuery.toString(), entityClass);
        query.setParameter("ids", ids);
        return query;
    }


    private StringBuilder initBaseFetchQuery(List<String> joinFields, Map<String, String> mainAliases) {
        StringBuilder mainQuery = new StringBuilder("SELECT DISTINCT e FROM ").append(entityName).append(" e");
        Map<String, List<String>> collectionGroups = new LinkedHashMap<>();

        if (joinFields != null) {
            for (String field : joinFields) {
                if (field == null || field.isBlank()) {
                    throw new IllegalArgumentException("Join field name must not be null or blank.");
                }

                String effectiveField = field;
                boolean isSingle = singleRelations.contains(effectiveField);
                boolean isCollection = !isSingle && collectionRelations.contains(effectiveField);

                if (!isSingle && !isCollection) {
                    effectiveField = resolveAndCacheRelation(field);
                    if (effectiveField == null) {
                        // Plain basic field on root entity (e.g. "name") -> skip join
                        continue;
                    }
                    isSingle = singleRelations.contains(effectiveField);
                }

                if (isSingle) {
                    appendJoinPath(mainQuery, effectiveField, mainAliases);
                } else {
                    String groupKey = findCollectionGroupKey(effectiveField);
                    int lastDot = groupKey.lastIndexOf('.');
                    if (lastDot != -1) {
                        // Always join the single-relation chain leading up to a nested collection
                        appendJoinPath(mainQuery, groupKey.substring(0, lastDot), mainAliases);
                    }
                    collectionGroups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(effectiveField);
                }
            }
        }

        // Join only the FIRST collection group that appears in joinFields; skip the rest for Hibernate fetch
        if (!collectionGroups.isEmpty()) {
            for (String path : collectionGroups.values().iterator().next()) {
                appendJoinPath(mainQuery, path, mainAliases);
            }
        }

        return mainQuery;
    }

    private String buildConditionClause(StringBuilder query, String cName, Object cValue, String paramName, Map<String, String> aliases) {
        if (cName.equals(idName) || cName.equals("id(e)")) {
            return "id(e)" + (cValue == null ? " IS NULL" : " = :" + paramName);
        }

        int lastDot = cName.lastIndexOf('.');
        if (lastDot == -1) {
            return "e." + cName + (cValue == null ? " IS NULL" : " = :" + paramName);
        }

        String relationPath = cName.substring(0, lastDot);
        String property = cName.substring(lastDot + 1);

        if (property.isBlank()) {
            throw new IllegalArgumentException("Invalid condition property in '" + cName + "'");
        }

        if (!aliases.containsKey(relationPath)) {
            if (!singleRelations.contains(relationPath) && !collectionRelations.contains(relationPath)) {
                resolveAndCacheRelation(relationPath);
            }
            appendJoinPath(query, relationPath, aliases);
        }

        String targetAlias = aliases.get(relationPath);
        return targetAlias + "." + property + (cValue == null ? " IS NULL" : " = :" + paramName);
    }

    /**
     * Dynamically validates each segment of a dotted path against the JPA Metamodel
     * and caches every valid relation prefix into singleRelations or collectionRelations.
     * - Basic fields are skipped (returning the relation prefix before the basic field, or null if root basic field).
     * - If a second collection appears in the path, stops before the second collection and returns the prefix up to the first.
     */
    private String resolveAndCacheRelation(String field) {
        ManagedType<?> currentType = this.entity;
        boolean seenCollection = false;
        String lastValidRelationPath = null;
        int start = 0;

        while (true) {
            int dotIdx = field.indexOf('.', start);
            String currentPath = (dotIdx == -1) ? field : field.substring(0, dotIdx);
            String segment = (dotIdx == -1) ? field.substring(start) : field.substring(start, dotIdx);

            if (segment.isBlank()) {
                throw new IllegalArgumentException("Invalid relation path '" + field + "' on " + entityName);
            }
            if (currentType == null) {
                throw new IllegalArgumentException(
                        "Cannot navigate further inside non-entity collection at '" + currentPath + "' in '" + field + "'");
            }

            Attribute<?, ?> attribute;
            try {
                attribute = currentType.getAttribute(segment);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "'" + segment + "' in '" + field + "' is not a valid field of "
                                + currentType.getJavaType().getSimpleName(), ex);
            }

            if (!attribute.isAssociation() && !attribute.isCollection()) {
                if (dotIdx != -1) {
                    throw new IllegalArgumentException(
                            "Cannot navigate inside basic field '" + currentPath + "' in '" + field + "'");
                }
                break;
            }

            if (attribute.isCollection()) {
                if (seenCollection) {
                    // Skip second collection in the same path; keep the valid prefix up to here
                    break;
                }
                seenCollection = true;
                collectionRelations.add(currentPath);
                lastValidRelationPath = currentPath;
                if (attribute instanceof PluralAttribute<?, ?, ?> plural
                        && plural.getElementType() instanceof ManagedType<?> nextType) {
                    currentType = nextType;
                } else {
                    currentType = null;
                }
            } else {
                if (seenCollection) {
                    collectionRelations.add(currentPath);
                } else {
                    singleRelations.add(currentPath);
                }
                lastValidRelationPath = currentPath;
                if (attribute instanceof SingularAttribute<?, ?> singular
                        && singular.getType() instanceof ManagedType<?> nextType) {
                    currentType = nextType;
                } else {
                    currentType = null;
                }
            }

            if (dotIdx == -1) {
                break;
            }
            start = dotIdx + 1;
        }

        return lastValidRelationPath;
    }

    private String findCollectionGroupKey(String field) {
        int start = 0;
        while (true) {
            int dotIdx = field.indexOf('.', start);
            if (dotIdx == -1) {
                return field;
            }
            String prefix = field.substring(0, dotIdx);
            if (collectionRelations.contains(prefix)) {
                return prefix;
            }
            start = dotIdx + 1;
        }
    }

    private void appendJoinPath(StringBuilder query, String field, Map<String, String> aliases) {
        String parentAlias = "e";
        int start = 0;

        while (true) {
            int dotIdx = field.indexOf('.', start);
            String currentPath = (dotIdx == -1) ? field : field.substring(0, dotIdx);
            String segment = (dotIdx == -1) ? field.substring(start) : field.substring(start, dotIdx);

            String alias = aliases.get(currentPath);
            if (alias == null) {
                alias = "j" + aliases.size();
                aliases.put(currentPath, alias);
                query.append(" LEFT JOIN FETCH ").append(parentAlias).append('.').append(segment).append(' ')
                        .append(alias);
            }
            parentAlias = alias;

            if (dotIdx == -1) {
                break;
            }
            start = dotIdx + 1;
        }
    }
}
