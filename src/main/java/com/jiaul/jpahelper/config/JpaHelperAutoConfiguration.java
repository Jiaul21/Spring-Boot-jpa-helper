package com.jiaul.jpahelper.config;

import com.jiaul.jpahelper.hibernate.HibernateFilterManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

/**
 * Spring Boot auto-configuration for {@code jpa-helper}.
 * <p>
 * Automatically executes after Spring Boot's JPA/Hibernate auto-configurations
 * ({@code HibernateJpaAutoConfiguration}) to initialize common infrastructure beans:
 * <ul>
 *   <li>{@link HibernateFilterManager}: Created with a thread-safe, transaction-aware shared {@link EntityManager} proxy
 *       to manage dynamic soft-delete filters.</li>
 *   <li>{@link BasicAuditorAware}: Default fallback {@link AuditorAware} bean providing {@code "SYSTEM"} as the auditor identifier.</li>
 * </ul>
 * <p>
 * Auto-configuration can be selectively enabled or disabled via {@code application.yaml}:
 * <pre>{@code
 * jpa-helper:
 *   hibernate-filter:
 *     enabled: true # default is true; set to false to disable
 * }</pre>
 */
@AutoConfiguration(afterName = {
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
@ConditionalOnClass({ EntityManager.class, Session.class })
@ConditionalOnProperty(prefix = "jpa-helper.hibernate-filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JpaHelperAutoConfiguration {

    /**
     * Registers the {@link HibernateFilterManager} bean if none is already declared by the application.
     * <p>
     * Creates a thread-safe shared {@link EntityManager} proxy via {@link SharedEntityManagerCreator#createSharedEntityManager}
     * connected to the application's primary {@link EntityManagerFactory}.
     *
     * @param entityManagerFactory the active JPA entity manager factory
     * @return a new {@link HibernateFilterManager} instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EntityManagerFactory.class)
    public HibernateFilterManager hibernateFilterManager(EntityManagerFactory entityManagerFactory) {
        EntityManager sharedEntityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        return new HibernateFilterManager(sharedEntityManager);
    }

    /**
     * Registers a fallback {@link AuditorAware} bean returning {@code "SYSTEM"} if the application
     * does not define its own custom {@code AuditorAware} bean.
     *
     * @return a {@link BasicAuditorAware} instance
     */
    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    @ConditionalOnClass(AuditorAware.class)
    public AuditorAware<String> defaultAuditorAware() {
        return new BasicAuditorAware();
    }
}
