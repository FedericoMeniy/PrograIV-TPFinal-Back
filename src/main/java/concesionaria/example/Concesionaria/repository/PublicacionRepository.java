package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.enums.EstadoPublicacion;
import concesionaria.example.Concesionaria.enums.TipoPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    List<Publicacion> findByVendedorId(Long idUsuario);
    List<Publicacion> findByEstadoAndTipoPublicacion(EstadoPublicacion estadp, TipoPublicacion tipo);
    List<Publicacion> findByEstado(EstadoPublicacion estado);
    long countByEstado(EstadoPublicacion estado);
    long countByTipoPublicacion(TipoPublicacion tipo);
    @Query("SELECT p FROM Publicacion p " +
            "WHERE p.estado = :estadoPub " +
            "AND p.tipoPublicacion = :tipoPub " +
            "AND NOT EXISTS (SELECT r FROM Reserva r WHERE r.publicacion = p AND r.estado = concesionaria.example.Concesionaria.enums.EstadoReserva.ACEPTADA)")
    List<Publicacion> findPublicacionesDisponibles(
            @Param("estadoPub") EstadoPublicacion estadoPub,
            @Param("tipoPub") TipoPublicacion tipoPub
    );
}
