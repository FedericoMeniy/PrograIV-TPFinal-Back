package concesionaria.example.Concesionaria.entity;

import io.jsonwebtoken.lang.Strings;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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


    @ElementCollection(fetch = FetchType.EAGER) // EAGER para que siempre cargue las fotos
    @CollectionTable(name="auto_imagenes", joinColumns=@JoinColumn(name="auto_id"))
    @Column(name="imagen_url")
    private List<String> imagenesUrl = new ArrayList<>();

    @OneToOne
    private FichaTecnica fichaTecnica;
    private Boolean vendido;


}
