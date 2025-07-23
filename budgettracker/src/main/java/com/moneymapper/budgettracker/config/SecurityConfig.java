package com.moneymapper.budgettracker.config;

import com.moneymapper.budgettracker.security.JwtAuthenticationFilter;
import com.moneymapper.budgettracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Profile("!dev")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final UserService userService;
        private final PasswordEncoder passwordEncoder;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                        .csrf(csrf -> csrf.disable())
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                // Public endpoints
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                                .requestMatchers("/h2-console/**").permitAll()

                                // Admin-only endpoints
                                .requestMatchers(HttpMethod.DELETE, "/api/expenses").hasRole("ADMIN")
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                // User endpoints
                                .requestMatchers("/api/budgets/**").hasRole("USER")
                                .requestMatchers("/api/expenses/**").hasRole("USER")
                                .requestMatchers("/api/categories/**").hasRole("USER")

                                // Any other request must be authenticated
                                .anyRequest().authenticated()
                        )
                        .authenticationProvider(authenticationProvider())
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


                return http.build();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userService);
                authProvider.setPasswordEncoder(passwordEncoder);
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}