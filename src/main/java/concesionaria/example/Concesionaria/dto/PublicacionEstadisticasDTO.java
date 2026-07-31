package concesionaria.example.Concesionaria.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class PublicacionEstadisticasDTO {
    private long totalPublicaciones;
    private long pendientes;
    private long aceptadas;
    private long rechazadas;
    private long usuario;
    private long concesionaria;
    private Map<String, Long> topMarcas;
}