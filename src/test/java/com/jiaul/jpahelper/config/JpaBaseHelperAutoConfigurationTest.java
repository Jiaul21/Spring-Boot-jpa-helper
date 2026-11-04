package com.jiaul.jpahelper.config;

import com.jiaul.jpahelper.hibernate.HibernateFilterManager;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class JpaBaseHelperAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JpaHelperAutoConfiguration.class));

    @Configuration
    static class MockEntityManagerConfig {
        @Bean
        public jakarta.persistence.EntityManagerFactory entityManagerFactory() {
            jakarta.persistence.EntityManagerFactory emf = Mockito.mock(jakarta.persistence.EntityManagerFactory.class);
            EntityManager em = Mockito.mock(EntityManager.class);
            Session session = Mockito.mock(Session.class);
            Mockito.when(emf.createEntityManager()).thenReturn(em);
            Mockito.when(em.unwrap(Session.class)).thenReturn(session);
            return emf;
        }
    }

    @Configuration
    static class CustomFilterManagerConfig {
        @Bean
        public HibernateFilterManager customFilterManager() {
            return new HibernateFilterManager(Mockito.mock(EntityManager.class));
        }
    }

    @Test
    @DisplayName("Should auto-configure HibernateFilterManager when EntityManager bean is present")
    void shouldAutoConfigureHibernateFilterManager() {
        contextRunner
                .withUserConfiguration(MockEntityManagerConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(HibernateFilterManager.class);
                });
    }

    @Test
    @DisplayName("Should not configure HibernateFilterManager when EntityManager bean is absent")
    void shouldNotConfigureWhenEntityManagerIsMissing() {
        contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(HibernateFilterManager.class);
                });
    }

    @Test
    @DisplayName("Should back off when jpa-helper.hibernate-filter.enabled is false")
    void shouldBackOffWhenPropertyDisabled() {
        contextRunner
                .withUserConfiguration(MockEntityManagerConfig.class)
                .withPropertyValues("jpa-helper.hibernate-filter.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(HibernateFilterManager.class);
                });
    }

    @Test
    @DisplayName("Should respect user-provided custom HibernateFilterManager bean")
    void shouldRespectUserDefinedBean() {
        contextRunner
                .withUserConfiguration(MockEntityManagerConfig.class, CustomFilterManagerConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(HibernateFilterManager.class);
                    assertThat(context).hasBean("customFilterManager");
                });
    }

    @Test
    @DisplayName("Should not configure when EntityManager or Session class is not on classpath")
    void shouldNotConfigureWhenClassMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(EntityManager.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(HibernateFilterManager.class);
                });
    }
}
