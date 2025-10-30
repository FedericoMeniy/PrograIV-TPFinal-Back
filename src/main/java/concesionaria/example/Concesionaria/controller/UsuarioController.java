package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.LoginUsuarioDTO;
import concesionaria.example.Concesionaria.dto.RegistroUsuarioDto;
import concesionaria.example.Concesionaria.dto.JwtResponseDTO; // Importar
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.service.JwtService; // Importar
import concesionaria.example.Concesionaria.service.UsuarioService;
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
    private final JwtService jwtService; // Inyectar

    @Autowired
    public UsuarioController(UsuarioService usuarioService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroUsuarioDto registroUsuarioDto){

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
            Usuario usuarioResponse = usuarioService.getUsuarioByEmail(usuarioLogueado.getEmail());

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
}