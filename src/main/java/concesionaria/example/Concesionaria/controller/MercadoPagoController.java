package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.service.ReservaService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificacion")
@Data
public class MercadoPagoController {

    private final ReservaService reservaService;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> recibirNotificacion(@RequestParam("reserva_id") Long reserva_id,@RequestParam("topic") String topic,@RequestParam("id") String id){
        if ("payment".equals(topic)) {
            reservaService.procesarNotificacionDePago(reserva_id, id);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
