package com.turkcell.soccer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    // Security filter for authentication users
    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable usage of cookies
                // Permit all users trying to authenticate force every request to be authenticated to continue
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(   // Allow swagger
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/account").permitAll() // Registration purposes
                        .requestMatchers("/auth").permitAll()
                        .anyRequest().authenticated()
                )
                // Stateless session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).addFilterBefore(
                        jwtAuthenticationFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                );
        // Return the security filter
        return http.build();
    }

    // Function to encode passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
