package concesionaria.example.Concesionaria.repository;

import concesionaria.example.Concesionaria.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository <Reserva,Long> {

}
