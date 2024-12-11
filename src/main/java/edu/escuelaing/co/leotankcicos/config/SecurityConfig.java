// package edu.escuelaing.co.leotankcicos.config;

// import java.util.Arrays;

// import org.apache.catalina.filters.CorsFilter;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// @Configuration
// public class SecurityConfig {

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration config = new CorsConfiguration();
//         config.setAllowedOrigins(Arrays.asList("https://frontarsw.z22.web.core.windows.net"));  // Orígenes permitidos
//         config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // Métodos permitidos
//         config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));  // Headers permitidos
//         config.setAllowCredentials(true);  // Permitir cookies o credenciales

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", config);
//         return source;
//     }
// }