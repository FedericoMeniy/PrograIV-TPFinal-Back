package concesionaria.example.Concesionaria.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Auto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String marca;
    private String modelo;
    private double precio;
    private Integer anio;
    private String km;
    private String color;

    @OneToOne
    private FichaTecnica fichaTecnica;
    private Boolean vendido;
}
