package com.uco.eoi.report;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Puts the report lock in front of every write URL of the entry screen. */
@Configuration
public class ReportLockConfig implements WebMvcConfigurer {

    private final JdbcTemplate jdbc;

    public ReportLockConfig(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ReportLockInterceptor(jdbc)).addPathPatterns(
                "/reports/*/edit", "/reports/*/prefill",
                "/reports/*/directors/**", "/reports/*/allied/**", "/reports/*/banks/**",
                "/reports/*/facilities/**", "/reports/*/derivatives/**",
                "/reports/*/currencies/**", "/reports/*/experience/**", "/reports/*/rubric/**");
    }
}
