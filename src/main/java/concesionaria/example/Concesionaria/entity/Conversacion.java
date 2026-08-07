package concesionaria.example.Concesionaria.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Conversacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long publicacionId;
    private String compradorEmail; // El que pregunta
    private String vendedorEmail;  // El dueño del auto (franapipi en tu foto)
}
