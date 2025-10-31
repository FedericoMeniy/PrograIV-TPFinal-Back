package concesionaria.example.Concesionaria.config;

import concesionaria.example.Concesionaria.enums.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// --- NUEVOS IMPORTS ---
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
// --- FIN NUEVOS IMPORTS ---


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // (El PasswordEncoder ya lo movimos a ApplicationConfig, eso está perfecto)

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())

                // --- 1. AÑADE ESTA LÍNEA ---
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- 2. AÑADE ESTA LÍNEA ---
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario/registro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/publicacion/crearPublicacion").authenticated()
                        .requestMatchers(HttpMethod.GET, "/publicacion/misPublicaciones").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/publicacion/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/publicacion/{id}").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/usuario/{id}").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // --- 3. AÑADE ESTE BEAN AL FINAL ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite peticiones desde tu frontend de Angular
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // Permite los métodos HTTP que usas
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permite todas las cabeceras (incluyendo 'Authorization' y 'Content-Type')
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Permite que el navegador envíe credenciales (como cookies o tokens)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración a todas las rutas de tu API
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}