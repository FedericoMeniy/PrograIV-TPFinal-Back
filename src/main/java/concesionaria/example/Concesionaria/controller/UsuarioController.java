package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.LoginUsuarioDTO;
import concesionaria.example.Concesionaria.dto.UsuarioRegistroDto;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired; // 1. IMPORTAR
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/usuario")

public class UsuarioController {

    private UsuarioService usuarioService;


    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody UsuarioRegistroDto registroUsuarioDto){

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
            Usuario usuarioLogueado = usuarioService.login(loginDto);
            return ResponseEntity.ok(usuarioLogueado);

            // NOTA: En un futuro, aquí deberías generar y devolver un JWT (JSON Web Token)
            // en lugar del objeto Usuario completo.

        } catch (RuntimeException e) {
            // Si el email no existe o la contraseña es incorrecta
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarNombreUsuario(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            // Obtenemos el "nombre" del cuerpo del JSON
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