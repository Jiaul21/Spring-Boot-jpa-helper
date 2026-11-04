package com.jiaul.jpahelper.config;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Fallback {@link AuditorAware} implementation supplying a default auditor username.
 * <p>
 * Always returns {@code "SYSTEM"} as the current auditor principal. This ensures that
 * {@link com.jiaul.jpahelper.entity.BaseEntity} audit attributes ({@code createdBy} and {@code updatedBy})
 * are consistently populated even when no security context or authenticated user is active
 * (such as background tasks, asynchronous workers, or unit tests).
 * <p>
 * Registered by {@link JpaHelperAutoConfiguration} with {@code @ConditionalOnMissingBean(AuditorAware.class)}.
 * If an application declares its own custom {@code AuditorAware} bean (e.g. resolving users from Spring Security),
 * this default bean automatically yields.
 */
public class BasicAuditorAware implements AuditorAware<String> {

    /**
     * Default auditor identifier returned when no security context is present.
     */
    public static final String DEFAULT_AUDITOR = "SYSTEM";

    /**
     * Returns the default auditor identifier wrapped in an {@link Optional}.
     *
     * @return an {@link Optional} containing {@value #DEFAULT_AUDITOR}
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(DEFAULT_AUDITOR);
    }
}
