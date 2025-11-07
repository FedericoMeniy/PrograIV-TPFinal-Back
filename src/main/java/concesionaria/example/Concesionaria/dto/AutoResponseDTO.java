package concesionaria.example.Concesionaria.dto;

import lombok.Data;

import java.util.List;

@Data
public class AutoResponseDTO {
    private Long id;
    private String marca;
    private String modelo;
    private double precio;
    private Integer anio;
    private String km;
    private String color;
    private FichaTecnicaResponseDTO fichaTecnica;
    private List<String> imagenesUrl;
}
