package edu.escuelaing.co.leotankcicos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Add global CORS configuration for HTTP endpoints (optional)
        registry.addMapping("/**")
                .allowedOrigins("https://frontarsw.z22.web.core.windows.net")  // Allowed origins for all HTTP endpoints
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}