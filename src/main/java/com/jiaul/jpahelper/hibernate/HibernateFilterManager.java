package com.jiaul.jpahelper.hibernate;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;

public class HibernateFilterManager {

    private static final DeleteFilter DEFAULT_DELETE_FILTER = new DeleteFilter();

    private final EntityManager entityManager;

    public HibernateFilterManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session session() {
        return entityManager.unwrap(Session.class);
    }

    public <T> void enableFilter(HibernateFilterable<T> filter, T value) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        session()
                .enableFilter(filter.getFilter())
                .setParameter(filter.getParameter(), value);
    }

    public <T> void disableFilter(HibernateFilterable<T> filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        session().disableFilter(filter.getFilter());
    }

    public boolean isFilterEnabled(HibernateFilterable<?> filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        return session().getEnabledFilter(filter.getFilter()) != null;
    }

    // Convenience methods for the built-in DeleteFilter (used by BaseEntity)

    public void enableDeletedFilter() {
        enableFilter(DEFAULT_DELETE_FILTER, false);
    }

    public void enableDeletedFilter(boolean deleted) {
        enableFilter(DEFAULT_DELETE_FILTER, deleted);
    }

    public void disableDeletedFilter() {
        disableFilter(DEFAULT_DELETE_FILTER);
    }

    public boolean isDeletedFilterEnabled() {
        return isFilterEnabled(DEFAULT_DELETE_FILTER);
    }
}