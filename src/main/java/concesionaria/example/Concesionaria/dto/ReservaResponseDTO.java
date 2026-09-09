package concesionaria.example.Concesionaria.dto;

import concesionaria.example.Concesionaria.enums.EstadoReserva;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservaResponseDTO {

    private Long id;
    private UsuarioReservaDTO usuarioReserva;
    private LocalDateTime fecha;
    private PublicacionResponseDTO publicacion;
    private double montoReserva;
    private EstadoReserva estadoReserva;
}
