package concesionaria.example.Concesionaria.dto;

import concesionaria.example.Concesionaria.enums.Rol;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
}