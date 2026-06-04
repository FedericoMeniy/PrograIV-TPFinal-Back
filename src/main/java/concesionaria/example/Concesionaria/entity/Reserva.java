package concesionaria.example.Concesionaria.entity;

import concesionaria.example.Concesionaria.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Publicacion publicacion;
    private LocalDateTime fecha;

    @ManyToOne
    private Auto idAuto;
    private EstadoReserva estado;
    private double montoReserva;
    private String paymentId;
}
