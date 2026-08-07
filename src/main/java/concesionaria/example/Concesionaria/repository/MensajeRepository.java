package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    // Trae el historial de la sala privada
    List<Mensaje> findByConversacionIdOrderByFechaAsc(Long conversacionId);
    Optional<Mensaje> findTopByConversacionIdOrderByFechaDesc(Long conversacionId);
}