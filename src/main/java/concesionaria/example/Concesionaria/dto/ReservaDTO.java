package concesionaria.example.Concesionaria.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservaDTO {

    private UsuarioReservaDTO usuarioReservaDTO;
    private Long idPublicacion;
    private LocalDateTime fecha;

}
