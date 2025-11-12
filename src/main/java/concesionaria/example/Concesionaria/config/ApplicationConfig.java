package concesionaria.example.Concesionaria.config;

// --- NUEVOS IMPORTS ---
import com.mercadopago.MercadoPagoConfig;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import concesionaria.example.Concesionaria.service.UsuarioService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// --- FIN IMPORTS ---

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

    @Value("${mercadopago.access-token.private}")
    private String privateAccessToken;


    @PostConstruct
    public void initializeMercadoPagoSDK() {

        MercadoPagoConfig.setAccessToken(privateAccessToken);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        // CORRECCIÓN: Creamos e inicializamos el UsuarioService con sus dependencias
        // Nota: Si el constructor de UsuarioService no usa el passwordEncoder, se puede omitir aquí.
        // Asumiendo que sí lo usa:
        return new UsuarioService(usuarioRepository, passwordEncoder);
    }

    // 2. Define el AuthenticationProvider
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Usa el UserDetailsService creado
        authProvider.setPasswordEncoder(passwordEncoder);       // Usa el PasswordEncoder
        return authProvider;
    }

}