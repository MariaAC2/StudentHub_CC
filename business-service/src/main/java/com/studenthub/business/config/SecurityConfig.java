package com.studenthub.business.config;

import com.studenthub.business.config.AuthServiceAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthServiceAuthenticationFilter authServiceAuthenticationFilter;

    public SecurityConfig(AuthServiceAuthenticationFilter authServiceAuthenticationFilter) {
        this.authServiceAuthenticationFilter = authServiceAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow CORS preflight
                        .requestMatchers(HttpMethod.POST, "/auth/register", "auth/login", "auth/validate").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authServiceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
