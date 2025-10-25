package concesionaria.example.Concesionaria.dto;

import concesionaria.example.Concesionaria.enums.EstadoPublicacion;
import concesionaria.example.Concesionaria.enums.TipoPublicacion;
import lombok.Data;

@Data
public class PublicacionResponseDTO {
    private Long id;
    private String descripcion;
    private EstadoPublicacion estado;
    private TipoPublicacion tipoPublicacion;
    private String nombreVendedor; // <-- ¡MUY IMPORTANTE! Solo el nombre, no el objeto Usuario
    private AutoResponseDTO auto; // DTO anidado
}
