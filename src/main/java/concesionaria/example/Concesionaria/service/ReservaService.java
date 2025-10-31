package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.dto.ReservaDTO;
import concesionaria.example.Concesionaria.dto.UsuarioReservaDTO;
import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.entity.Reserva;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.enums.EstadoReserva;
import concesionaria.example.Concesionaria.repository.PublicacionRepository;
import concesionaria.example.Concesionaria.repository.ReservaRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservaService {

    private PublicacionRepository publicacionRepository;
    private ReservaRepository reservaRepository;

    @Autowired
    public ReservaService(PublicacionRepository publicacionRepository, ReservaRepository reservaRepository) {
        this.publicacionRepository = publicacionRepository;
        this.reservaRepository = reservaRepository;
    }


    @Transactional
    public Reserva cargarReserva(ReservaDTO reservaDTO){

        Reserva nuevaReserva = new Reserva();
        Usuario usuario = new Usuario();
        UsuarioReservaDTO usuarioReservaDTO = new UsuarioReservaDTO(reservaDTO.getUsuarioReservaDTO());


        Publicacion publicacion = publicacionRepository.findById(reservaDTO.getIdPublicacion()).orElseThrow(()-> new RuntimeException("La publicacion no existe"));

        usuario.setNombre(usuarioReservaDTO.getNombre());
        usuario.setTelefono(usuarioReservaDTO.getTelefono());
        usuario.setEmail(usuarioReservaDTO.getEmail());

        nuevaReserva.setUsuario(usuario);
        nuevaReserva.setPublicacion(publicacion);
        nuevaReserva.setEstado(EstadoReserva.PENDIENTE);
        nuevaReserva.setFecha(LocalDateTime.now());

        return reservaRepository.save(nuevaReserva);
    }

    public List<ReservaDTO> obtenerReservasPorUsuario(Long idUsuario){

        List<Reserva> reservasUsuario = reservaRepository.findByUsuarioId(idUsuario);
        List<ReservaDTO> reservasUsuarioDTO = new ArrayList<>();

        for(Reserva reserva : reservasUsuario){

            reservasUsuarioDTO.add(entityReservaToReservaDTO(reserva));
        }

        return reservasUsuarioDTO;
    }

    public ReservaDTO entityReservaToReservaDTO(Reserva reserva){

        ReservaDTO reservaDTO = new ReservaDTO();
        UsuarioReservaDTO usuarioReservaDTO = new UsuarioReservaDTO();
        Usuario usuario = reserva.getUsuario();


        reservaDTO.setEstadoReserva(reserva.getEstado());

        usuarioReservaDTO.setNombre(usuario.getNombre());
        usuarioReservaDTO.setEmail(usuario.getEmail());
        usuarioReservaDTO.setTelefono(usuario.getTelefono());

        reservaDTO.setUsuarioReservaDTO(usuarioReservaDTO);
        reservaDTO.setFecha(reserva.getFecha());
        reservaDTO.setIdPublicacion(reserva.getPublicacion().getId());

        return reservaDTO;
    }
}
