package concesionaria.example.Concesionaria.config;

import concesionaria.example.Concesionaria.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority; // Importar
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Importar
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays; // Importar
import java.util.Collection; // Importar
import java.util.stream.Collectors; // Importar

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Soy yo devuelta mas cosas de roles - authority

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Se mantiene el constructor original
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

        // Permitir que las solicitudes OPTIONS (preflight de CORS) pasen
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {

                // --- INICIO DE LA CORRECCIÓN CRÍTICA: LECTURA DE ROLES DESDE JWT ---

                // 1. Extraer la cadena de roles (ej: "ADMIN,USUARIO") del claim 'authority' del token
                String authoritiesString = jwtService.extractClaim(jwt, claims -> claims.get("authority", String.class));

                Collection<? extends GrantedAuthority> authorities;

                if (authoritiesString != null && !authoritiesString.isEmpty()) {
                    // 2. Convertir la cadena de roles a objetos GrantedAuthority
                    authorities = Arrays.stream(authoritiesString.split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                } else {
                    // Fallback: Si no hay roles en el token (lo cual no debería ocurrir con admin), usar los roles de la DB
                    authorities = userDetails.getAuthorities();
                }

                // 3. Crear objeto de autenticación para Spring Security usando las autoridades extraídas del JWT
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities // <--- AHORA USA LAS AUTORIDADES DEL TOKEN
                );
                // 4. Establecer detalles y autenticar
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}