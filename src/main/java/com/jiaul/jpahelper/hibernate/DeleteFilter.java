package com.jiaul.jpahelper.hibernate;

/**
 * Built-in {@link HibernateFilterable} implementation for soft-delete filtering.
 * <p>
 * Binds to {@code "deletedFilter"} on entities extending {@link com.jiaul.jpahelper.entity.BaseEntity}.
 * When activated with {@code false}, soft-deleted rows ({@code deleted = true}) are automatically excluded
 * from all select queries within the current Hibernate session.
 */
public class DeleteFilter implements HibernateFilterable<Boolean> {

    /**
     * Unique Hibernate filter definition name.
     */
    public static final String FILTER = "deletedFilter";

    /**
     * Parameter name for the boolean delete status.
     */
    public static final String PARAMETER = "deleted";

    /**
     * SQL/HQL condition applied by the filter.
     */
    public static final String CONDITION = "deleted = :deleted";

    /**
     * Returns the filter name {@value #FILTER}.
     *
     * @return the filter name
     */
    @Override
    public String getFilter() {
        return FILTER;
    }

    /**
     * Returns the parameter name {@value #PARAMETER}.
     *
     * @return the parameter name
     */
    @Override
    public String getParameter() {
        return PARAMETER;
    }

    /**
     * Returns the condition expression {@value #CONDITION}.
     *
     * @return the SQL/HQL condition string
     */
    @Override
    public String getCondition() {
        return CONDITION;
    }
}
