package com.verbitsky.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.verbitsky.property.WebClientProperties;
import com.verbitsky.security.CustomAuthFilter;

@Configuration
@EnableConfigurationProperties(WebClientProperties.class)
@EnableWebSecurity
@EnableMethodSecurity()
class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, CustomAuthFilter authFilter,
            @Qualifier("customAuthProvider") AuthenticationProvider authProvider) throws Exception {

        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(HttpMethod.POST, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/**").permitAll()
                .anyRequest().authenticated());

        http.sessionManagement(configureSessionManagement());
        http.authenticationProvider(authProvider);

        http.csrf(configureCsrf());
        http.cors(configureCors());

        http.addFilterBefore(authFilter, BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }

    private Customizer<SessionManagementConfigurer<HttpSecurity>> configureSessionManagement() {
        return configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private static Customizer<CorsConfigurer<HttpSecurity>> configureCors() {
        return AbstractHttpConfigurer::disable;
    }

    private static Customizer<CsrfConfigurer<HttpSecurity>> configureCsrf() {
        return AbstractHttpConfigurer::disable;
    }
}