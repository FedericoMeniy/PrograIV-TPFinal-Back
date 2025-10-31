package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.ReservaDTO;
import concesionaria.example.Concesionaria.entity.Reserva;
import concesionaria.example.Concesionaria.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reserva")
public class ReservaController {

    private ReservaService reservaService;

    @Autowired
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> cargarReserva(@Valid@RequestBody ReservaDTO reservaDTO){

        try{

            Reserva reservaCargada = reservaService.cargarReserva(reservaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservaDTO);

        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }


    }
}
