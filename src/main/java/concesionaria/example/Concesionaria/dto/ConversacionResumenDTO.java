package concesionaria.example.Concesionaria.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversacionResumenDTO {
    private Long conversacionId;
    private Long publicacionId;

    // Aca guardamos el email de la OTRA persona (para saber con quien hablo)
    private String emailContacto;

    // Para que Angular sepa si en este chat soy el duenio del auto o el interesado
    private String rol; // "COMPRADOR" o "VENDEDOR"
    private String nombreContacto;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private int cantidadNoLeidos;
}