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

                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())

                // No usar sesiones (stateless JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Deshabilitar login de formulario y OAuth2 login automatico
                // (manejamos OAuth2 manualmente via GoogleTokenVerifierService)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .oauth2Login(oauth2 -> oauth2.disable())

                // Retornar 401 JSON en vez de redirigir a Google para requests no autenticados
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"No autenticado. Token JWT requerido.\"");
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publicacion/tienda").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publicacion/usados").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publicacion/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                        .requestMatchers("/ws-chat/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario/registro", "/usuario/login", "/notificacion/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/publicacion/crearPublicacion", "/reserva/crear").authenticated()
                        .requestMatchers("/publicacion/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/reserva/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/usuario/login/google", "/usuario/registro/google").permitAll()
                        .requestMatchers("/usuario/login", "/usuario/registro").permitAll()
                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite peticiones desde tu frontend de Angular y cualquier puerto local
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*"));

        // Permite los métodos HTTP que usas
        //FEDE ACA AGREGUE PATCH PARA CAMBIAR EL ESTADO DE LAS PUBLICACIONES A PENDIENTES (EL ADMIN)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

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