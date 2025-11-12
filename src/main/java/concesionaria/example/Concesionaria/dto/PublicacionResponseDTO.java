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
    private String nombreVendedor;
    private String vendedorTelefono;
    private AutoResponseDTO auto;
}
