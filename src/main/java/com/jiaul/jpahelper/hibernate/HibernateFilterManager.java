package com.jiaul.jpahelper.hibernate;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;

/**
 * Manager component for controlling dynamic Hibernate filters on the active persistence context.
 * <p>
 * Provides type-safe methods to enable, disable, and check the status of any {@link HibernateFilterable}
 * filter on the underlying Hibernate {@link Session}, along with built-in convenience methods for
 * soft-delete filtering via {@link DeleteFilter}.
 */
public class HibernateFilterManager {

    private static final DeleteFilter DEFAULT_DELETE_FILTER = new DeleteFilter();

    private final EntityManager entityManager;

    /**
     * Constructs a new {@code HibernateFilterManager} using the provided JPA {@link EntityManager}.
     *
     * @param entityManager the JPA entity manager to unwrap Hibernate sessions from
     */
    public HibernateFilterManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Unwraps and returns the current Hibernate {@link Session}.
     *
     * @return the active Hibernate session
     */
    private Session session() {
        return entityManager.unwrap(Session.class);
    }

    // -------------------------------------------------------------
    // Generic Filter Controls
    // -------------------------------------------------------------

    /**
     * Enables a Hibernate filter and binds its parameter value on the active session.
     *
     * @param <T>    the parameter data type
     * @param filter the filter definition to enable
     * @param value  the parameter value to bind to the filter
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    public <T> void enableFilter(HibernateFilterable<T> filter, T value) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        session()
                .enableFilter(filter.getFilter())
                .setParameter(filter.getParameter(), value);
    }

    /**
     * Disables a Hibernate filter on the active session.
     *
     * @param <T>    the parameter data type
     * @param filter the filter definition to disable
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    public <T> void disableFilter(HibernateFilterable<T> filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        session().disableFilter(filter.getFilter());
    }

    /**
     * Checks whether a specific Hibernate filter is currently enabled on the active session.
     *
     * @param filter the filter definition to check
     * @return {@code true} if the filter is enabled, {@code false} otherwise
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    public boolean isFilterEnabled(HibernateFilterable<?> filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        return session().getEnabledFilter(filter.getFilter()) != null;
    }

    // -------------------------------------------------------------
    // Soft-Delete Filter Convenience Methods
    // -------------------------------------------------------------

    /**
     * Enables the soft-delete filter with {@code deleted = false}, automatically excluding
     * all soft-deleted records from queries in the current session.
     */
    public void enableDeletedFilter() {
        enableFilter(DEFAULT_DELETE_FILTER, false);
    }

    /**
     * Enables the soft-delete filter with a specific boolean flag.
     * <p>
     * Passing {@code true} restricts queries to return <em>only</em> soft-deleted records (e.g., for trash/recycle bin views).
     *
     * @param deleted {@code false} to exclude deleted rows; {@code true} to include only deleted rows
     */
    public void enableDeletedFilter(boolean deleted) {
        enableFilter(DEFAULT_DELETE_FILTER, deleted);
    }

    /**
     * Disables the soft-delete filter, allowing queries to return both active and soft-deleted records.
     */
    public void disableDeletedFilter() {
        disableFilter(DEFAULT_DELETE_FILTER);
    }

    /**
     * Checks if the soft-delete filter is currently active in the current session.
     *
     * @return {@code true} if soft-delete filtering is active, {@code false} otherwise
     */
    public boolean isDeletedFilterEnabled() {
        return isFilterEnabled(DEFAULT_DELETE_FILTER);
    }
}
