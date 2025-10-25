package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    List<Publicacion> findByVendedorId(Long idUsuario);
}
