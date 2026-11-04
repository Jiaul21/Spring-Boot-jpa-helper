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

@AutoConfiguration(afterName = {
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
@ConditionalOnClass({ EntityManager.class, Session.class })
@ConditionalOnProperty(prefix = "jpa-helper.hibernate-filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JpaHelperAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EntityManagerFactory.class)
    public HibernateFilterManager hibernateFilterManager(EntityManagerFactory entityManagerFactory) {
        EntityManager sharedEntityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        return new HibernateFilterManager(sharedEntityManager);
    }

    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    @ConditionalOnClass(AuditorAware.class)
    public AuditorAware<String> defaultAuditorAware() {
        return new BasicAuditorAware();
    }
}
