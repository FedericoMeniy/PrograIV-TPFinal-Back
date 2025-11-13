package concesionaria.example.Concesionaria.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors; // Importar

@Service
public class JwtService {
    //Esto lo cambie porque si el front  no recibe el Rol no se puede autenticar que sea un admin o no - Fd

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.ms}")
    private long expirationMs;

    // --- Extracción de información del token ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --- Generación del token ---

    public String generateToken(UserDetails userDetails) {
        // [MODIFICACIÓN] Crear claims y agregar la autoridad (Rol)
        Map<String, Object> claims = new HashMap<>();

        // Extraer las autoridades/roles y ponerlas en el token
        // Spring Security leerá esto para hasAuthority()
        String authority = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // Si hay múltiples roles, los une

        // Es importante que el claim del rol tenga el nombre 'authority' o similar
        // para que JwtAuthenticationFilter lo reconozca. Usaremos 'authority' o lo pasamos directamente
        // como una lista si usas el método de extracción de roles de Spring.

        // Para simplificar y dado que solo tienes un rol, podemos pasarlo como un claim.
        // Pero lo más robusto es usar las authorities para crear el token.

        // En tu caso, es más limpio pasar las authorities como una lista o un string
        claims.put("authority", authority);

        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // --- Validación del token ---

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // --- Obtener la clave de firma ---

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}