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

                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .oauth2Login(oauth2 -> oauth2.disable())

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
                        .requestMatchers(HttpMethod.GET, "/publicacion/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                        .requestMatchers("/ws-chat/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario/registro", "/usuario/login", "/notificacion/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/publicacion/crearPublicacion", "/reserva/crear").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/publicacion/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/publicacion/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/usuario/cuenta").authenticated()
                        .requestMatchers("/publicacion/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/reserva/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/usuario/login/google", "/usuario/registro/google").permitAll()
                        .requestMatchers("/usuario/login", "/usuario/registro").permitAll()
                        .requestMatchers("/conversacion/**").authenticated()
                        // Nuevo recuperar cuenta //
                        .requestMatchers(HttpMethod.POST, "/usuario/olvide-password", "/usuario/restablecer-password").permitAll()
                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Agrego link de Vercel
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*", "https://my-car-tesis-front-rouge.vercel.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
