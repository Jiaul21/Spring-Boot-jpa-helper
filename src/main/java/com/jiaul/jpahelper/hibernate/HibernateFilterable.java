package com.jiaul.jpahelper.hibernate;

/**
 * Contract for defining type-safe Hibernate filters.
 * <p>
 * Implementing classes define the filter name, parameter name, and SQL/HQL condition
 * configured in entity {@link org.hibernate.annotations.FilterDef} and {@link org.hibernate.annotations.Filter}
 * annotations, allowing dynamic activation via {@link HibernateFilterManager}.
 *
 * @param <T> the data type of the filter parameter value (e.g., {@link Boolean}, {@link Long}, {@link String})
 */
public interface HibernateFilterable<T> {

    /**
     * Returns the unique filter name matching {@code @FilterDef(name = ...)}.
     *
     * @return the Hibernate filter name
     */
    String getFilter();

    /**
     * Returns the parameter name matching {@code @ParamDef(name = ...)}.
     *
     * @return the parameter name used in the filter condition
     */
    String getParameter();

    /**
     * Returns the condition expression applied by the filter (e.g., {@code "deleted = :deleted"}).
     *
     * @return the SQL/HQL filter condition string
     */
    String getCondition();
}
