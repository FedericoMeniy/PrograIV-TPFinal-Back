package concesionaria.example.Concesionaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioReservaDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El mail es obligatorio")
    @Email
    private String email;
    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;

    public UsuarioReservaDTO(UsuarioReservaDTO usuarioReservaDTO) {
        this.nombre = usuarioReservaDTO.nombre;
        this.email = usuarioReservaDTO.email;
        this.telefono = usuarioReservaDTO.telefono;
    }
}
