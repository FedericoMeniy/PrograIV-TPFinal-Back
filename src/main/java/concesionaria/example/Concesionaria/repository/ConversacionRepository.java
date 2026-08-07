package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {
    // Busca si ya existe un chat entre este comprador y esta publicación
    Optional<Conversacion> findByPublicacionIdAndCompradorEmail(Long publicacionId, String compradorEmail);
    List<Conversacion> findByCompradorEmailOrVendedorEmail(String compradorEmail, String vendedorEmail);
}