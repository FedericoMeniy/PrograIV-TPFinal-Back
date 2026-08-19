package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.enums.EstadoPublicacion;
import concesionaria.example.Concesionaria.enums.TipoPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    @Query("SELECT p FROM Publicacion p JOIN FETCH p.auto a JOIN FETCH p.vendedor v LEFT JOIN FETCH a.fichaTecnica f WHERE p.vendedor.id = :idUsuario")
    List<Publicacion> findByVendedorId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT p FROM Publicacion p JOIN FETCH p.auto a JOIN FETCH p.vendedor v LEFT JOIN FETCH a.fichaTecnica f WHERE p.estado = :estado AND p.tipoPublicacion = :tipo")
    List<Publicacion> findByEstadoAndTipoPublicacion(@Param("estado") EstadoPublicacion estado, @Param("tipo") TipoPublicacion tipo);

    @Query("SELECT p FROM Publicacion p JOIN FETCH p.auto a JOIN FETCH p.vendedor v LEFT JOIN FETCH a.fichaTecnica f WHERE p.estado = :estado")
    List<Publicacion> findByEstado(@Param("estado") EstadoPublicacion estado);

    long countByEstado(EstadoPublicacion estado);
    long countByTipoPublicacion(TipoPublicacion tipo);

    @Query("SELECT p FROM Publicacion p " +
            "JOIN FETCH p.auto a " +
            "JOIN FETCH p.vendedor v " +
            "LEFT JOIN FETCH a.fichaTecnica f " +
            "WHERE p.estado = :estadoPub " +
            "AND p.tipoPublicacion = :tipoPub " +
            "AND NOT EXISTS (SELECT r FROM Reserva r WHERE r.publicacion = p AND r.estado = concesionaria.example.Concesionaria.enums.EstadoReserva.ACEPTADA)")
    List<Publicacion> findPublicacionesDisponibles(
            @Param("estadoPub") EstadoPublicacion estadoPub,
            @Param("tipoPub") TipoPublicacion tipoPub
    );

    @Query("SELECT a.marca, COUNT(p) FROM Publicacion p JOIN p.auto a WHERE p.estado = concesionaria.example.Concesionaria.enums.EstadoPublicacion.ACEPTADA GROUP BY a.marca ORDER BY COUNT(p) DESC")
    List<Object[]> findTopMarcas();

    @Modifying
    @Query(value = "DELETE FROM usuario_favoritos WHERE publicacion_id = :publicacion_id", nativeQuery = true)
    void eliminarDeTodosLosFavoritos(@Param("publicacion_id") Long idPublicacion);

    @Modifying
    @Query(value = "DELETE FROM reserva WHERE publicacion_id = :publicacion_id", nativeQuery = true)
    void eliminarReservasDePublicacion(@Param("publicacion_id") Long idPublicacion);
}
