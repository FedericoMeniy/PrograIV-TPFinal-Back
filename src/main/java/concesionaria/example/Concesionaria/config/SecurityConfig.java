package concesionaria.example.Concesionaria.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas (sin autenticación)
                        .requestMatchers(HttpMethod.OPTIONS).permitAll() // Permite preflight
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publicacion/tienda").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publicacion/usados").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publicacion/{id}").permitAll() // Ver detalles de publicación
                        .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario/registro", "/usuario/login", "/notificacion/**").permitAll()

                        // Rutas de administrador (solo ADMIN)
                        .requestMatchers("/publicacion/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/reserva/admin/**").hasAuthority("ROLE_ADMIN")

                        // Rutas que requieren autenticación (USUARIO o ADMIN)
                        .requestMatchers(HttpMethod.POST, "/publicacion/crearPublicacion").hasAnyAuthority("ROLE_USUARIO", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/publicacion/misPublicaciones").hasAnyAuthority("ROLE_USUARIO", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/publicacion/**").hasAnyAuthority("ROLE_USUARIO", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/publicacion/**").hasAnyAuthority("ROLE_USUARIO", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/publicacion/**").hasAnyAuthority("ROLE_USUARIO", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/reserva/crear").hasAnyAuthority("ROLE_USUARIO", "ROLE_ADMIN")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permite peticiones desde tu frontend de Angular
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // Permite los métodos HTTP que usas
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Permite todas las cabeceras solicitadas por el cliente, incluyendo 'Authorization'
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));

        // Permite que el navegador envíe credenciales (como cookies o tokens)
        configuration.setAllowCredentials(true);

        // Permite exponer headers personalizados
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Aplica esta configuración a todas las rutas de tu API
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
