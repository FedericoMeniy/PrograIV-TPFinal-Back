package concesionaria.example.Concesionaria.dto;

import concesionaria.example.Concesionaria.enums.EstadoReserva;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReservaResponseDTO {

    private Long id;
    private UsuarioReservaDTO usuarioReserva;
    private LocalDateTime fecha;
    private Long idPublicacion;
    private double montoReserva;
    private EstadoReserva estadoReserva;
}
