package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.ReservaRequestDTO;
import concesionaria.example.Concesionaria.dto.ReservaResponseDTO;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import concesionaria.example.Concesionaria.service.ReservaService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reserva")
public class ReservaController {
    private final ReservaService reservaService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/crear")
    public ResponseEntity<?> iniciarReserva(@Valid@RequestBody ReservaRequestDTO reservaRequestDTO){

        try{
            String redireccionURL = reservaService.iniciarReserva(reservaRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(redireccionURL);

        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/mis-reservas")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerMisReservas(Authentication authentication){
        try {
            // 1. Obtener el email del usuario autenticado
            String emailUsuario = authentication.getName(); //

            // 2. Obtener el ID del usuario a partir del email
            Usuario usuario = usuarioRepository.findByemail(emailUsuario).orElseThrow(() -> new RuntimeException("Usuario no encontrado."));; //
            Long idUsuario = usuario.getId(); //

            // 3. Obtener las reservas usando el ID del usuario
            List<ReservaResponseDTO> reservas = reservaService.obtenerReservasPorUsuario(idUsuario);

            return ResponseEntity.ok(reservas);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/admin/lista")
    public List<ReservaResponseDTO> obtenerTodas(){
        return reservaService.getReservas();
    }

    @PutMapping("/modificar-reserva")
    public ResponseEntity<?> modificarReserva(@Valid @RequestBody ReservaResponseDTO reservaDTO) {
        try {
            ReservaResponseDTO reservaActualizada = reservaService.modificarReserva(reservaDTO);
            return ResponseEntity.ok(reservaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarReserva(@PathVariable Long id) {
        try {
            reservaService.eliminarReserva(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}