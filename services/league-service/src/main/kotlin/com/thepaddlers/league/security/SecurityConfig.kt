package com.thepaddlers.league.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(private val jwtFilter: JwtFilter) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
            .requestMatchers("/api/booking/**").hasAnyRole("SUPER_ADMIN", "CLUB_MANAGER", "STAFF", "PLAYER")
            .requestMatchers("/api/court/**").hasAnyRole("SUPER_ADMIN", "CLUB_MANAGER", "STAFF")
            .requestMatchers("/api/user/**").hasAnyRole("SUPER_ADMIN", "CLUB_MANAGER")
            .anyRequest().authenticated()
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
