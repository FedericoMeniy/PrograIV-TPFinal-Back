package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Reserva;
import concesionaria.example.Concesionaria.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository <Reserva,Long> {
    List<Reserva> findByUsuarioId(Long idUsuario);
    List<Reserva> findByPublicacion_Id(Long publicacionId);
    // Antes era: Optional<Reserva> findByPublicacionIdAndEstado(...)
    // AHORA TIENE QUE SER ASÍ:
    List<Reserva> findByPublicacionIdAndEstado(Long publicacionId, EstadoReserva estado);
}
