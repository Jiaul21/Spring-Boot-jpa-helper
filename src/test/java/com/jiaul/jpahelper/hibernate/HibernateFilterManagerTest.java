package com.jiaul.jpahelper.hibernate;

import jakarta.persistence.EntityManager;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class HibernateFilterManagerTest {

    private EntityManager entityManager;
    private Session session;
    private Filter filter;
    private HibernateFilterManager filterManager;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        session = mock(Session.class);
        filter = mock(Filter.class);

        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.enableFilter(anyString())).thenReturn(filter);
        when(filter.setParameter(anyString(), any())).thenReturn(filter);

        filterManager = new HibernateFilterManager(entityManager);
    }

    @Test
    @DisplayName("Should enable generic filter with parameter value")
    void shouldEnableGenericFilter() {
        DeleteFilter deleteFilter = new DeleteFilter();
        filterManager.enableFilter(deleteFilter, false);

        verify(session).enableFilter(DeleteFilter.FILTER);
        verify(filter).setParameter(DeleteFilter.PARAMETER, false);
    }

    @Test
    @DisplayName("Should disable generic filter")
    void shouldDisableGenericFilter() {
        DeleteFilter deleteFilter = new DeleteFilter();
        filterManager.disableFilter(deleteFilter);

        verify(session).disableFilter(DeleteFilter.FILTER);
    }

    @Test
    @DisplayName("Should check whether generic filter is enabled")
    void shouldCheckIfFilterEnabled() {
        DeleteFilter deleteFilter = new DeleteFilter();

        when(session.getEnabledFilter(DeleteFilter.FILTER)).thenReturn(filter);
        assertThat(filterManager.isFilterEnabled(deleteFilter)).isTrue();

        when(session.getEnabledFilter(DeleteFilter.FILTER)).thenReturn(null);
        assertThat(filterManager.isFilterEnabled(deleteFilter)).isFalse();
    }

    @Test
    @DisplayName("Should enable deleted filter with default false (non-deleted records only)")
    void shouldEnableDeletedFilterDefault() {
        filterManager.enableDeletedFilter();

        verify(session).enableFilter(DeleteFilter.FILTER);
        verify(filter).setParameter(DeleteFilter.PARAMETER, false);
    }

    @Test
    @DisplayName("Should enable deleted filter with explicit boolean value")
    void shouldEnableDeletedFilterExplicit() {
        filterManager.enableDeletedFilter(true);

        verify(session).enableFilter(DeleteFilter.FILTER);
        verify(filter).setParameter(DeleteFilter.PARAMETER, true);
    }

    @Test
    @DisplayName("Should disable deleted filter using convenience method")
    void shouldDisableDeletedFilterConvenience() {
        filterManager.disableDeletedFilter();

        verify(session).disableFilter(DeleteFilter.FILTER);
    }

    @Test
    @DisplayName("Should check whether deleted filter is enabled")
    void shouldCheckIfDeletedFilterEnabled() {
        when(session.getEnabledFilter(DeleteFilter.FILTER)).thenReturn(filter);
        assertThat(filterManager.isDeletedFilterEnabled()).isTrue();

        when(session.getEnabledFilter(DeleteFilter.FILTER)).thenReturn(null);
        assertThat(filterManager.isDeletedFilterEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when filter is null")
    void shouldThrowWhenFilterIsNull() {
        assertThatThrownBy(() -> filterManager.enableFilter(null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Filter must not be null");

        assertThatThrownBy(() -> filterManager.disableFilter(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Filter must not be null");

        assertThatThrownBy(() -> filterManager.isFilterEnabled(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Filter must not be null");
    }
}
