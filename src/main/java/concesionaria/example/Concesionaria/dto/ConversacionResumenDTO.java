package concesionaria.example.Concesionaria.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversacionResumenDTO {
    private Long conversacionId;
    private Long publicacionId;

    // Acá guardamos el email de la OTRA persona (para saber con quién hablo)
    private String emailContacto;

    // Para que Angular sepa si en este chat soy el dueño del auto o el interesado
    private String rol; // "COMPRADOR" o "VENDEDOR"
    private String nombreContacto;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
}