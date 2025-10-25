package concesionaria.example.Concesionaria.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class FichaTecnica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String motor;
    private String combustible;
    private String caja;
    private String puertas;
    private String potencia;

}
