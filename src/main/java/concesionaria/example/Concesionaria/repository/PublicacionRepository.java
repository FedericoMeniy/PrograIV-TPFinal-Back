package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.enums.EstadoPublicacion;
import concesionaria.example.Concesionaria.enums.TipoPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    List<Publicacion> findByVendedorId(Long idUsuario);
    List<Publicacion> findByEstadoAndTipoPublicacion(EstadoPublicacion estadp, TipoPublicacion tipo);
    List<Publicacion> findByEstado(EstadoPublicacion estado);
}
