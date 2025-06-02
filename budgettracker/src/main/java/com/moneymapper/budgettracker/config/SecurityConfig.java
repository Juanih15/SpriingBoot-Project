package com.moneymapper.budgettracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Profile("!dev")
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        PasswordEncoder password() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        SecurityFilterChain api(HttpSecurity http) throws Exception {
                return http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/register", "/api/login").permitAll()
                                                .anyRequest().authenticated())
                                .httpBasic(Customizer.withDefaults()) // swap for JWT later
                                .build();
        }
}
