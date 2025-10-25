package concesionaria.example.Concesionaria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Define el Bean de PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Define la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 3. Aplica la configuración de CORS definida abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 4. Deshabilita CSRF (común para APIs stateless)
                .csrf(csrf -> csrf.disable())

                // 5. Define las reglas de autorización
                .authorizeHttpRequests(authz -> authz
                        // 6. PERMITE explícitamente todas las peticiones OPTIONS (pre-vuelo)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 7. PERMITE el registro
                        .requestMatchers("/usuario/registro").permitAll()

                        // 8. (Opcional) Protege el resto de endpoints
                        // .anyRequest().authenticated()

                        // Por ahora, para probar, puedes dejar todo abierto
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    // 9. Define la configuración de CORS global
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 10. Permite peticiones desde tu frontend de Angular
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // 11. Permite los métodos HTTP que usará tu frontend
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 12. Permite todos los encabezados
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 13. (Opcional) Permite credenciales si usas cookies/sesiones
        // configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}