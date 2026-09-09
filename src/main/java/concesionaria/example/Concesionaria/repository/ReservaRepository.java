package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Reserva;
import concesionaria.example.Concesionaria.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository <Reserva,Long> {
    List<Reserva> findByUsuarioId(Long idUsuario);
    List<Reserva> findByPublicacion_Id(Long publicacionId);
    List<Reserva> findByPublicacionIdAndEstado(Long publicacionId, EstadoReserva estado);
    List<Reserva> findByEstadoAndFechaBefore(EstadoReserva estado, LocalDateTime fecha);
}
