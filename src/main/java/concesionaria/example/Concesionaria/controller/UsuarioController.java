package concesionaria.example.Concesionaria.controller;

 // Importar
import concesionaria.example.Concesionaria.dto.JwtResponseDTO;
import concesionaria.example.Concesionaria.dto.LoginUsuarioDTO;
import concesionaria.example.Concesionaria.dto.RegistroUsuarioDTO;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import concesionaria.example.Concesionaria.service.JwtService; // Importar
import concesionaria.example.Concesionaria.service.UsuarioService;
import concesionaria.example.Concesionaria.service.GoogleTokenVerifierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // Importar
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Importar
import org.springframework.security.core.Authentication; // Importar
import org.springframework.security.core.AuthenticationException; // Importar
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager; // Inyectar
    private final JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    @Autowired // Spring usará este constructor para inyectar todas las dependencias
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
            // 1. Autenticar credenciales con AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            // 2. Obtener el UserDetails (que es el Usuario)
            Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();

            // 3. Generar el JWT
            String token = jwtService.generateToken(usuarioLogueado);

            // 4. Obtener el objeto Usuario sin el password (o solo los datos a devolver)
            Usuario usuarioResponse = usuarioRepository.findByemail(usuarioLogueado.getEmail()).orElseThrow(() -> new RuntimeException("Usuario no encontrado."));;;

            // 5. Devolver la respuesta con el token y datos del usuario
            JwtResponseDTO jwtResponse = JwtResponseDTO.builder()
                    .token(token)
                    .id(usuarioResponse.getId())
                    .nombre(usuarioResponse.getNombre())
                    .email(usuarioResponse.getEmail())
                    .rol(usuarioResponse.getRol())
                    .build();

            return ResponseEntity.ok(jwtResponse);

        } catch (AuthenticationException e) {
            // Si las credenciales son incorrectas
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email o contraseña incorrecta.");
        } catch (RuntimeException e) {
            // Manejo de errores genéricos (e.g., usuario no encontrado, aunque AuthenticationException debería cubrirlo)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarNombreUsuario(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String nuevoNombre = body.get("nombre");

            if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre no puede estar vacío");
            }

            Usuario usuarioActualizado = usuarioService.actualizarNombre(id, nuevoNombre);
            return ResponseEntity.ok(usuarioActualizado);

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

            // Verificar el token de Google
            Map<String, Object> payload = googleTokenVerifierService.verifyToken(idToken);
            String email = (String) payload.get("email");
            String nombre = (String) payload.get("name");

            // Buscar usuario por email
            Usuario usuario = usuarioRepository.findByemail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado. Por favor regístrate primero."));

            // Generar token JWT
            String token = jwtService.generateToken(usuario);

            // Crear respuesta
            JwtResponseDTO jwtResponse = JwtResponseDTO.builder()
                    .token(token)
                    .id(usuario.getId())
                    .nombre(usuario.getNombre())
                    .email(usuario.getEmail())
                    .rol(usuario.getRol())
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

            // Verificar el token de Google
            Map<String, Object> payload = googleTokenVerifierService.verifyToken(idToken);
            String email = (String) payload.get("email");
            String nombre = (String) payload.get("name");

            // Verificar si el usuario ya existe
            if (usuarioRepository.findByemail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El usuario ya está registrado. Por favor inicia sesión.");
            }

            String telefono = request.get("telefono");
            String password = request.get("password");

            // Crear nuevo usuario SIN establecer el rol
            // El servicio UsuarioService debe establecer el rol como "USER" automáticamente
            RegistroUsuarioDTO registroDto = new RegistroUsuarioDTO();
            registroDto.setNombre(nombre != null ? nombre : email.split("@")[0]);
            registroDto.setEmail(email);
            registroDto.setTelefono(telefono != null && !telefono.isEmpty() ? telefono : "");
            registroDto.setPassword(password != null && !password.isEmpty() ? password : "");

            // IMPORTANTE: NO establecer rol aquí
            // El servicio UsuarioService debe ignorar cualquier rol que venga en el DTO
            // y siempre establecer "USER" por defecto

            // Registrar usuario - el servicio establecerá el rol como "USER"
            Usuario usuarioRegistrado = usuarioService.registrarUsuario(registroDto);

            // Generar token JWT
            String token = jwtService.generateToken(usuarioRegistrado);

            // Crear respuesta
            JwtResponseDTO jwtResponse = JwtResponseDTO.builder()
                    .token(token)
                    .id(usuarioRegistrado.getId())
                    .nombre(usuarioRegistrado.getNombre())
                    .email(usuarioRegistrado.getEmail())
                    .rol(usuarioRegistrado.getRol()) // Será "USER" siempre
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
}