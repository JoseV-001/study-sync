package com.josev001.study_sync.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://192.168.0.49:3001",
                        "http://192.168.0.49:3002",
                        "http://192.168.*:*",
                        "http://100.101.154.61:*",
                        "http://100.*:*",
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "*"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
