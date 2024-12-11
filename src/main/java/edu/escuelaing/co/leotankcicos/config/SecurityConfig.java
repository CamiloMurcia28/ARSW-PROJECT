// package edu.escuelaing.co.leotankcicos.config;

// import java.util.Arrays;

// import org.apache.catalina.filters.CorsFilter;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
// import org.springframework.security.oauth2.jwt.JwtDecoder;
// import org.springframework.security.oauth2.jwt.JwtDecoders;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//             .authorizeRequests()
//                 .anyRequest().permitAll() // Permite todas las solicitudes (sin autenticación)
//             .and()
//             .formLogin().disable() // Deshabilita el inicio de sesión basado en formulario
//             .httpBasic().disable(); // Deshabilita la autenticación básica HTTP
//     return http.build();
// }

    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     CorsConfiguration config = new CorsConfiguration();
    //     config.setAllowedOrigins(Arrays.asList("https://frontarsw.z22.web.core.windows.net"));  // Orígenes permitidos
    //     config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // Métodos permitidos
    //     config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));  // Headers permitidos
    //     config.setAllowCredentials(true);  // Permitir cookies o credenciales

    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", config);
    //     return source;
    // }
//}