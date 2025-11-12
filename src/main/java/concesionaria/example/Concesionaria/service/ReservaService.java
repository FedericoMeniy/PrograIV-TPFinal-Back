package concesionaria.example.Concesionaria.service;

import com.mercadopago.resources.payment.Payment;
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
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@RequiredArgsConstructor
public class ReservaService {
    private final PublicacionRepository publicacionRepository;
    private final ReservaRepository reservaRepository;
    private final MercadoPagoService mercadoPagoService;

    @Transactional
    public String iniciarReserva(ReservaDTO reservaDTO){

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
        nuevaReserva.setMontoReserva(publicacion.getAuto().getPrecio()*0.90);

        Reserva reservaPreGuardada = reservaRepository.save(nuevaReserva);

        String pagoURL = mercadoPagoService.crearPreferenciaDePago(reservaPreGuardada.getPublicacion(),reservaPreGuardada.getId(),reservaPreGuardada.getMontoReserva());

        return pagoURL;
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

    @Transactional
    public void procesarNotificacionDePago(Long reservaId, String paymentId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElse(null);

        if (reserva == null) {
            System.err.println("Webhook - Reserva ID " + reservaId + " no encontrada. ID de Pago: " + paymentId);
            return;
        }

        try {
            // 1. Obtener los detalles del pago desde la API de Mercado Pago
            Payment payment = mercadoPagoService.obtenerDetallesDePago(paymentId);
            String estadoMP = payment.getStatus().toString();

            System.out.println("Pago ID: " + paymentId + " - Estado de MP: " + estadoMP);

            // 2. Actualizar el estado de la Reserva en la base de datos
            if ("approved".equalsIgnoreCase(estadoMP)) {
                reserva.setEstado(EstadoReserva.ACEPTADA);
                reserva.setPaymentId(paymentId);

            } else if ("rejected".equalsIgnoreCase(estadoMP) || "cancelled".equalsIgnoreCase(estadoMP)) {
                reserva.setEstado(EstadoReserva.CANCELADA);

            } else if ("pending".equalsIgnoreCase(estadoMP)) {
                reserva.setEstado(EstadoReserva.PENDIENTE);
            }

            reservaRepository.save(reserva);

        } catch (Exception e) {
            System.err.println("Error al procesar la notificación de pago para Reserva ID " + reservaId + ": " + e.getMessage());
        }
    }

    public List<ReservaDTO> getReservas(){
        List<Reserva> reservas = reservaRepository.findAll();

        return reservas.stream()
                .map(this::entityReservaToReservaDTO)
                .collect(Collectors.toList());
    }
}
