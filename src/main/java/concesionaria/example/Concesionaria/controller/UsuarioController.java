package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.*;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import concesionaria.example.Concesionaria.service.GoogleTokenVerifierService;
import concesionaria.example.Concesionaria.service.JwtService;
import concesionaria.example.Concesionaria.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, AuthenticationManager authenticationManager, JwtService jwtService, UsuarioRepository usuarioRepository, GoogleTokenVerifierService googleTokenVerifierService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.googleTokenVerifierService = googleTokenVerifierService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroUsuarioDTO registroUsuarioDto){
        try{
            Usuario usuarioRegistrado = usuarioService.registrarUsuario(registroUsuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRegistrado);
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@Valid @RequestBody LoginUsuarioDTO loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();

            String token = jwtService.generateToken(usuarioLogueado);

            Usuario usuarioResponse = usuarioRepository.findByemail(usuarioLogueado.getEmail()).orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

            JwtResponseDTO jwtResponse = JwtResponseDTO.builder()
                    .token(token)
                    .id(usuarioResponse.getId())
                    .nombre(usuarioResponse.getNombre())
                    .email(usuarioResponse.getEmail())
                    .rol(usuarioResponse.getRol())
                    .telefono(usuarioResponse.getTelefono())
                    .build();

            return ResponseEntity.ok(jwtResponse);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email o contraseña incorrecta.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEmailUsuario(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String nuevoEmail = body.get("email");

            if (nuevoEmail == null || nuevoEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El email no puede estar vacío");
            }

            Usuario usuarioActualizado = usuarioService.actualizarEmail(id, nuevoEmail);

            String nuevoToken = jwtService.generateToken(usuarioActualizado);

            JwtResponseDTO jwtResponse = JwtResponseDTO.builder()
                    .token(nuevoToken)
                    .id(usuarioActualizado.getId())
                    .nombre(usuarioActualizado.getNombre())
                    .email(usuarioActualizado.getEmail())
                    .rol(usuarioActualizado.getRol())
                    .build();

            return ResponseEntity.ok(jwtResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/login/google")
    public ResponseEntity<?> loginConGoogle(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("idToken");
            if (idToken == null || idToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El token de Google es requerido");
            }

            Map<String, Object> payload = googleTokenVerifierService.verifyToken(idToken);
            String email = (String) payload.get("email");
            String nombre = (String) payload.get("name");

            Usuario usuario = usuarioRepository.findByemail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado. Por favor regístrate primero."));

            String token = jwtService.generateToken(usuario);

            JwtResponseDTO jwtResponse = JwtResponseDTO.builder()
                    .token(token)
                    .id(usuario.getId())
                    .nombre(capitalize(usuario.getNombre()))
                    .email(usuario.getEmail())
                    .rol(usuario.getRol())
                    .telefono(usuario.getTelefono())
                    .build();

            return ResponseEntity.ok(jwtResponse);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    @PostMapping("/registro/google")
    public ResponseEntity<?> registroConGoogle(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("idToken");
            if (idToken == null || idToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El token de Google es requerido");
            }

            Map<String, Object> payload = googleTokenVerifierService.verifyToken(idToken);
            String email = (String) payload.get("email");
            String nombre = (String) payload.get("name");

            if (usuarioRepository.findByemail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El usuario ya está registrado. Por favor inicia sesión.");
            }

            String password = java.util.UUID.randomUUID().toString();

            RegistroUsuarioDTO registroDto = new RegistroUsuarioDTO();
            registroDto.setNombre(capitalize(nombre != null ? nombre : email.split("@")[0]));
            registroDto.setEmail(email);
            registroDto.setTelefono(null);
            registroDto.setPassword(password);

            Usuario usuarioRegistrado = usuarioService.registrarUsuario(registroDto);

            String token = jwtService.generateToken(usuarioRegistrado);

            JwtResponseDTO jwtResponse = JwtResponseDTO.builder()
                    .token(token)
                    .id(usuarioRegistrado.getId())
                    .nombre(usuarioRegistrado.getNombre())
                    .email(usuarioRegistrado.getEmail())
                    .rol(usuarioRegistrado.getRol())
                    .telefono(usuarioRegistrado.getTelefono())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(jwtResponse);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("ya está registrado") || e.getMessage().contains("CONFLICT")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al registrar usuario: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) return text;
        String[] words = text.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    @PostMapping("/completar-telefono")
    public ResponseEntity<?> completarTelefono(@RequestParam String email, @RequestParam String telefono){
        if(usuarioRepository.existsByTelefono(telefono)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El telefono ya esta registrado.");
        }

        Usuario usuario = usuarioRepository.findByemail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setTelefono(telefono);

        usuarioRepository.save(usuario);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/favoritos/{idPublicacion}")
    public ResponseEntity<?> toggleFavorito(@PathVariable Long idPublicacion, Authentication authentication) {
        usuarioService.toggleFavorito(authentication.getName(), idPublicacion);
        return ResponseEntity.ok(Map.of("mensaje", "Favorito actualizado"));
    }

    @GetMapping("/favoritos")
    public ResponseEntity<List<PublicacionResponseDTO>> getFavoritos(Authentication authentication) {
        List<PublicacionResponseDTO> favoritos = usuarioService.getFavoritos(authentication.getName());
        return ResponseEntity.ok(favoritos);
    }

    @DeleteMapping("/cuenta")
    public ResponseEntity<?> eliminarFavorito(Authentication authentication){
        try{
            usuarioService.eliminarCuenta(authentication.getName());
            return ResponseEntity.ok(Map.of("mensaje", "Cuenta eliminada correctamente."));
        }catch(Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/olvide-password")
    public ResponseEntity<?> olvidePassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El email es requerido");
            }
            usuarioService.enviarCorreoRecuperacion(email);
            return ResponseEntity.ok(Map.of("mensaje", "Si el correo está registrado, recibirás las instrucciones."));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("mensaje", "Si el correo está registrado, recibirás las instrucciones."));
        }
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<?> restablecerPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String nuevaPassword = request.get("nuevaPassword");

            if (token == null || nuevaPassword == null) {
                return ResponseEntity.badRequest().body("Faltan datos requeridos.");
            }
            usuarioService.restablecerPassword(token, nuevaPassword);
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
