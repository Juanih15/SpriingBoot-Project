package com.moneymapper.budgettracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Dev‐only security configuration:
 * - Enables H2 console at /h2-console
 * - Requires HTTP Basic for any other endpoint
 * - Uses the default in‐memory user from application-dev.yml (dev/dev).
 */
@Configuration
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
