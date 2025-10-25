package concesionaria.example.Concesionaria.entity;

import concesionaria.example.Concesionaria.enums.Rol;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String password;

    @OneToMany(mappedBy = "vendedor")
    private List<Publicacion> publicaciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Reserva> reservas = new ArrayList<>();
    private Rol rol;
}
