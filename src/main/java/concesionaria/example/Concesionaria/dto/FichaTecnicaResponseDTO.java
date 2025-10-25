package concesionaria.example.Concesionaria.dto;

import lombok.Data;

@Data
public class FichaTecnicaResponseDTO {
    private Long id;
    private String motor;
    private String combustible;
    private String caja;
    private String puertas;
    private String potencia;
}
