package com.iab.devops.config;

import io.micrometer.common.lang.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer cors() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200", "http://127.0.0.1:4200")
                        .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS","HEAD")
                        .allowedHeaders("*")
                        .exposedHeaders("Content-Disposition", "Location") // <- necessário p/ pegar o filename
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
}
