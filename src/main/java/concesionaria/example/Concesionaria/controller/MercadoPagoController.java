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
        // Solo procesamos si es una notificación de pago.
        if ("payment".equals(topic)) {
            // Pasamos el ID de la reserva y el ID de pago de Mercado Pago al servicio
            reservaService.procesarNotificacionDePago(reserva_id, id);
        }

        // ¡CRUCIAL! Se debe devolver un 200 OK para evitar que Mercado Pago reintente la notificación
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
