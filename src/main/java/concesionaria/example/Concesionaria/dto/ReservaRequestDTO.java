package concesionaria.example.Concesionaria.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservaRequestDTO {

    @NotBlank
    private UsuarioReservaDTO usuarioReservaDTO;
    @NotBlank
    private Long idPublicacion;
    @NotBlank
    private LocalDateTime fecha;
}
