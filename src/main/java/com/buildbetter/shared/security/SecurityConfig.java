package com.buildbetter.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import com.buildbetter.shared.util.JwtUtil;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize etc.
public class SecurityConfig {

        @Value("${jwt.secret}") // long random string
        private String secret;

        @Bean
        public JwtUtil jwtUtil() {
                return new JwtUtil(secret);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http,
                        JwtUtil util) throws Exception {

                // -> Permit every request through the servlet layer.
                // Whether it is ALLOWED later depends on annotations.
                http.csrf(csrf -> csrf.disable())
                                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(new JwtAuthFilter(util),
                                                AuthorizationFilter.class)
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(new RestAuthEntryPoint())
                                                .accessDeniedHandler(new RestAccessDeniedHandler()))
                                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

                return http.build();
        }
}
