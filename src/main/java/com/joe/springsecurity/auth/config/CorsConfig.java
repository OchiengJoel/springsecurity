package com.joe.springsecurity.auth.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow CORS requests from your frontend
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")  // Your frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")  // Allowed HTTP methods
                .allowedHeaders("*");  // Allow all headers
    }
}