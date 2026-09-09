package concesionaria.example.Concesionaria.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversacionResumenDTO {
    private Long conversacionId;
    private Long publicacionId;
    private String emailContacto;
    private String rol;
    private String nombreContacto;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private int cantidadNoLeidos;
}