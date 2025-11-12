package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.ReservaDTO;
import concesionaria.example.Concesionaria.entity.Reserva;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import concesionaria.example.Concesionaria.service.ReservaService;
import concesionaria.example.Concesionaria.service.UsuarioService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reserva")
public class ReservaController {
    private final ReservaService reservaService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/crear")
    public ResponseEntity<?> iniciarReserva(@Valid@RequestBody ReservaDTO reservaDTO){

        try{
            String redireccionURL = reservaService.iniciarReserva(reservaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(redireccionURL);

        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/mis-reservas")
    public ResponseEntity<List<ReservaDTO>> obtenerMisReservas(Authentication authentication){
        try {
            // 1. Obtener el email del usuario autenticado
            String emailUsuario = authentication.getName(); //

            // 2. Obtener el ID del usuario a partir del email
            Usuario usuario = usuarioRepository.findByemail(emailUsuario).orElseThrow(() -> new RuntimeException("Usuario no encontrado."));; //
            Long idUsuario = usuario.getId(); //

            // 3. Obtener las reservas usando el ID del usuario
            List<ReservaDTO> reservas = reservaService.obtenerReservasPorUsuario(idUsuario);

            return ResponseEntity.ok(reservas);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/admin/lista")
    public List<ReservaDTO> obtenerTodas(){
        return reservaService.getReservas();
    }
}
