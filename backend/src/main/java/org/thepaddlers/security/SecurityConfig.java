package org.thepaddlers.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Give this security configuration an explicit bean name to avoid colliding with
// org.thepaddlers.config.SecurityConfig which previously also registered as 'securityConfig'.
@Configuration("jwtSecurityConfig")
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public DevAuthFilter devAuthFilter() {
        return new DevAuthFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/payments/**").authenticated()
                .anyRequest().permitAll()
            );
        // register dev filter and jwt filter before username/password auth
        http.addFilterBefore(devAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
