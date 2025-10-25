package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    // (RF02) Para listar "mis publicaciones"
    // Busca todas las publicaciones donde el ID del vendedor coincida
    List<Publicacion> findByVendedorId(Long vendedorId);

    // (RF01 - Seguridad) Para modificar o borrar "mis publicaciones"
    // Busca una publicación por su ID Y el ID del vendedor
    Optional<Publicacion> findByIdVendedorId(Long id, Long vendedorId);
}
