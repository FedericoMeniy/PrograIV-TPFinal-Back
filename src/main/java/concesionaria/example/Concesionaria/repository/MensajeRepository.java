package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Mensaje;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    // Trae el historial de la sala privada
    List<Mensaje> findByConversacionIdOrderByFechaAsc(Long conversacionId);
    Optional<Mensaje> findTopByConversacionIdOrderByFechaDesc(Long conversacionId);

    @Query("SELECT COUNT(m) FROM Mensaje m JOIN Conversacion c ON m.conversacionId = c.id " +
            "WHERE (c.compradorEmail = :email OR c.vendedorEmail = :email) " +
            "AND m.remitenteEmail != :email AND m.leido = false")
    long contarMensajesNoLeidos(@Param("email") String email);

    @Modifying
    @Query("UPDATE Mensaje m SET m.leido = true WHERE m.conversacionId = :conversacionId AND m.remitenteEmail != :email")
    void marcarComoLeidos(@Param("conversacionId") Long conversacionId, @Param("email") String email);


}