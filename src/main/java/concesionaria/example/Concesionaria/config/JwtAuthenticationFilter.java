package concesionaria.example.Concesionaria.config;

import concesionaria.example.Concesionaria.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Las solicitudes OPTIONS (preflight CORS) ya son manejadas por CorsFilterConfig
        // y nunca llegan hasta aquí.

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt);
            log.debug("[JWT] Email extraido del token: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                boolean valid = jwtService.isTokenValid(jwt, userDetails);
                log.debug("[JWT] Token valido para {}: {}", userEmail, valid);

                if (valid) {
                    String authoritiesString = jwtService.extractClaim(jwt, claims -> claims.get("authority", String.class));
                    log.debug("[JWT] Authorities en token: {}", authoritiesString);

                    Collection<? extends GrantedAuthority> authorities;

                    if (authoritiesString != null && !authoritiesString.isEmpty()) {
                        authorities = Arrays.stream(authoritiesString.split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                    } else {
                        authorities = userDetails.getAuthorities();
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("[JWT] Autenticacion establecida para: {}", userEmail);
                }
            }
        } catch (JwtException e) {
            // Token inválido, malformado o con firma incorrecta — limpiamos el contexto
            log.warn("[JWT] Token JWT inválido o expirado: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            // Error de infraestructura (BD remota caída, timeout de conexión, etc.)
            // NO limpiamos el contexto de seguridad — simplemente logueamos el error
            log.error("[JWT] ERROR al validar token (posible problema de BD/red): {} - {}",
                    e.getClass().getSimpleName(), e.getMessage(), e);
            // No hacemos clearContext() aquí para no destruir autenticaciones previas
            // Spring Security rechazará el request si el endpoint requiere auth
        }

        filterChain.doFilter(request, response);
    }
}