package concesionaria.example.Concesionaria.entity;

import concesionaria.example.Concesionaria.enums.EstadoPublicacion;
import concesionaria.example.Concesionaria.enums.TipoPublicacion;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario vendedor;

    @OneToOne
    private Auto auto;
    private String descripcion;
    private EstadoPublicacion estado;
    private TipoPublicacion tipoPublicacion;
}
