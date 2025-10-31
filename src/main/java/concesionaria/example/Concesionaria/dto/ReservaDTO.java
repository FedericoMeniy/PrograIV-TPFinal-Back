package concesionaria.example.Concesionaria.dto;

import concesionaria.example.Concesionaria.enums.EstadoReserva;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservaDTO {

    @NotBlank
    private UsuarioReservaDTO usuarioReservaDTO;
    @NotBlank
    private Long idPublicacion;
    @NotBlank
    private LocalDateTime fecha;
    @NotBlank
    private EstadoReserva estadoReserva;

}
