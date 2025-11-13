package concesionaria.example.Concesionaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservaRequestDTO {

    @NotNull
    private UsuarioReservaDTO usuarioReservaDTO;
    @NotNull
    private Long idPublicacion;
    @NotNull
    private LocalDateTime fecha;
}
