package com.joe.springsecurity.auth.config;

import com.joe.springsecurity.auth.filter.JwtAuthenticationFilter;
import com.joe.springsecurity.auth.service.UserDetailsServiceImp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)  // Enable @PreAuthorize
public class SecurityConfig {

    private final UserDetailsServiceImp userDetailsServiceImp;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomLogoutHandler logoutHandler;

    public SecurityConfig(UserDetailsServiceImp userDetailsServiceImp,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomLogoutHandler logoutHandler) {
        this.userDetailsServiceImp = userDetailsServiceImp;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors().and() // Enable CORS
                .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF for non-browser clients
                .authorizeRequests()
                .antMatchers("/api/v2/auth/register", "/api/v2/auth/login", "/api/v2/auth/refresh_token", "/api/v2/reset-password", "/api/v2/request-password-reset") // Allow authentication-related endpoints
                .permitAll()
                .antMatchers("/api/v2/inventoryitem/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "USER") // Allow Super_Admin, Admin and User to access inventory (read-only for User)
                .antMatchers(HttpMethod.POST, "/api/v2/inventoryitem/**").hasAnyRole("SUPER_ADMIN", "ADMIN") // Super_Admin, Admin only for creating inventory
                .antMatchers(HttpMethod.PUT, "/api/v2/inventoryitem/**").hasAnyRole("SUPER_ADMIN", "ADMIN") // Super_Admin, Admin only for updating inventory
                .antMatchers(HttpMethod.DELETE, "/api/v2/inventoryitem/**").hasAnyRole("SUPER_ADMIN", "ADMIN") // Super_Admin, Admin only for deleting inventory
                .anyRequest().authenticated() // Require authentication for all other endpoints
                .and()
                .userDetailsService(userDetailsServiceImp)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Stateless authentication (JWT)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Add JWT filter before authentication
                .exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> response.setStatus(403)) // Handle access denial
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // Handle unauthorized access
                .and()
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(logoutHandler) // Handle logout
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()) // Clear security context after logout
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Use BCrypt for password encoding
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();  // Provide authentication manager
    }
}
