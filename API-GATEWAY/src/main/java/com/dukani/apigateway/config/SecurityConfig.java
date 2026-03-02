package com.dukani.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(Collections.singletonList(frontendUrl));
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(List.of("*"));
                    corsConfiguration.setExposedHeaders(List.of("X-Error", "Authorization", "Content-Type"));
                    corsConfiguration.setAllowCredentials(true);

                    return corsConfiguration;
                }))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        //ADD MORE SERVICES HERE
                        .pathMatchers("/user/**").permitAll()     // USER-SERVICE
                        .pathMatchers("/product/**").permitAll()  // PRODUCTS-SERVICE


                        .pathMatchers("/admin/**").hasAuthority("ADMIN")
                        .pathMatchers("/user/**").hasAuthority("USER")
                        .anyExchange().authenticated()
                )
                .build();
    }
}
