package it.asso.core.config;

import it.asso.core.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disabilita CSRF (non serve per API stateless JWT)
                .csrf(csrf -> csrf.disable())

                // Gestione CORS (per permettere chiamate da Angular se su porta diversa in dev)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of("http://localhost:4200")); // O "*" per test
                    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    configuration.setAllowedHeaders(List.of("*"));
                    return configuration;
                }))

                // Gestione Sessione: STATELESS (non salvare cookie, usa solo il token)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Regole di Autorizzazione
                .authorizeHttpRequests(auth -> auth
                        // Endpoint Pubblici
                        .requestMatchers("/api/public/**", "/manorg/api/public/**").permitAll()
                        // Asset Statici (Angular)
                        .requestMatchers("/", "/index.html", "/assets/**", "/media/**", "/*.js", "/*.css", "/*.ico", "/favicon.ico", "/images/**").permitAll()
                        // Endpoint di sistema (opzionali)
                        .requestMatchers("/error").permitAll()
                        // Tutto il resto richiede autenticazione
                        .anyRequest().authenticated()
                );

        // Aggiungi il nostro filtro JWT prima del filtro standard
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean necessario per iniettare l'AuthenticationManager nei controller (se servirà in futuro)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}