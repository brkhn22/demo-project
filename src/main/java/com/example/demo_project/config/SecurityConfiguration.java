package com.example.demo_project.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                // Allow OPTIONS requests (for CORS)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Allow Swagger UI and API docs
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs.yaml").permitAll()
                
                // Allow authentication endpoints (these don't require JWT)
                .requestMatchers("/auth/authenticate").permitAll()
                .requestMatchers("/auth/activation/**").permitAll()
                .requestMatchers("/auth/forgot-password").permitAll()
                .requestMatchers("/auth/activate-forgot-password").permitAll()
                .requestMatchers("/auth/reset-password").permitAll()
                
                // Protected endpoints that require JWT
                .requestMatchers("/user/**").hasAnyAuthority("User", "Admin","Manager")
                .requestMatchers("/admin/**").hasAuthority("Admin")
                .requestMatchers("/auth/register").hasAuthority("Admin")
                .requestMatchers("/company/**").hasAuthority("Admin")
                .requestMatchers("/department/**").hasAuthority("Admin")
                .requestMatchers("/town/**").hasAuthority("Admin")
                .requestMatchers("/manager/**").hasAuthority("Manager")
                .requestMatchers( "/auth/register-by-manager").hasAnyAuthority("Manager")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    configuration.addAllowedOriginPattern("*");
    
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));
    
    // TRY BEING EXPLICIT FOR DIAGNOSIS:
    configuration.setAllowedHeaders(Arrays.asList(
        "Authorization", 
        "Content-Type", 
        "Accept", 
        "Origin", 
        "Access-Control-Request-Method", 
        "Access-Control-Request-Headers",
        "X-Requested-With", // Common header
        "ngrok-skip-browser-warning" // If your Flutter app might send this
        // Add any other specific custom headers your Flutter app sends
    ));
    // If the above explicit list works, then "*" wasn't catching something,
    // or a header was being sent that you weren't aware of.
    // If it still fails, the issue is likely with Origin or Methods.
    
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    
    return source;
    }
}
