package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository <Reserva,Long> {
    List<Reserva> findByUsuarioId(Long idUsuario);
    List<Reserva> findByPublicacion_Id(Long publicacionId);
}
