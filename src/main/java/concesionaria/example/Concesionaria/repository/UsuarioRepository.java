package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.dto.RegistroUsuarioDto;
import concesionaria.example.Concesionaria.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByemail(String email);
}

