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

    @ManyToOne
    @JoinColumn(name = "auto_id")
    private Auto idAuto;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;

    private LocalDateTime fecha;

    @Column(nullable = false)
    private double montoReserva;
    private String paymentId;
}
