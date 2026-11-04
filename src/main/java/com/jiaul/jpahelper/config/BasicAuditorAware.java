package com.jiaul.jpahelper.config;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class BasicAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("SYSTEM");
    }
}