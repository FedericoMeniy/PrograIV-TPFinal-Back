package concesionaria.example.Concesionaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistroUsuarioDto {

    @NotBlank(message = "El nombre no puede estar vacio")
    private String nombre;
    @NotBlank(message = "La contraseña no puede estar vacia")
    private String password;
    @NotBlank(message = "El mail no puede estar vacia")
    @Email(message = "El mail no tiene el formato correcto")
    private String email;
}
