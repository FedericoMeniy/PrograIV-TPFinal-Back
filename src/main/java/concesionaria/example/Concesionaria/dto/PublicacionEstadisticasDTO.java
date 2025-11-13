package concesionaria.example.Concesionaria.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicacionEstadisticasDTO {
    private long totalPublicaciones;
    private long pendientes;
    private long aceptadas;
    private long rechazadas;
    private long usuario;
    private long concesionaria;
}