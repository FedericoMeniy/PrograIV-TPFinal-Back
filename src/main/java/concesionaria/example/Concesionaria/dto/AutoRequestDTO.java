package concesionaria.example.Concesionaria.dto;

import lombok.Data;

@Data
public class AutoRequestDTO {
    private String marca;
    private String modelo;
    private double precio;
    private Integer anio;
    private String km;
    private String color;
    private FichaTecnicaRequestDTO fichaTecnica;
}
