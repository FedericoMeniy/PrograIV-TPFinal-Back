package concesionaria.example.Concesionaria.service;

import com.mercadopago.resources.payment.Payment;
import concesionaria.example.Concesionaria.dto.ReservaRequestDTO;
import concesionaria.example.Concesionaria.dto.ReservaResponseDTO;
import concesionaria.example.Concesionaria.dto.UsuarioReservaDTO;
import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.entity.Reserva;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.enums.EstadoReserva;
import concesionaria.example.Concesionaria.repository.PublicacionRepository;
import concesionaria.example.Concesionaria.repository.ReservaRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public String iniciarReserva(ReservaRequestDTO reservaRequestDTO){

        Reserva nuevaReserva = new Reserva();
        Usuario usuario = new Usuario();
        UsuarioReservaDTO usuarioReservaDTO = new UsuarioReservaDTO(reservaRequestDTO.getUsuarioReservaDTO());

        Publicacion publicacion = publicacionRepository.findById(reservaRequestDTO.getIdPublicacion()).orElseThrow(()-> new RuntimeException("La publicacion no existe"));

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

    public List<ReservaResponseDTO> obtenerReservasPorUsuario(Long idUsuario){

        List<Reserva> reservasUsuario = reservaRepository.findByUsuarioId(idUsuario);
        List<ReservaResponseDTO> reservasUsuarioDTO = new ArrayList<>();

        for(Reserva reserva : reservasUsuario){

            reservasUsuarioDTO.add(entityReservaToReservaDTO(reserva));
        }

        return reservasUsuarioDTO;
    }

    public ReservaResponseDTO entityReservaToReservaDTO(Reserva reserva){

        ReservaResponseDTO reservaResponseDTO = new ReservaResponseDTO();
        UsuarioReservaDTO usuarioReservaDTO = new UsuarioReservaDTO();
        Usuario usuario = reserva.getUsuario();

        reservaResponseDTO.setEstadoReserva(reserva.getEstado());

        usuarioReservaDTO.setNombre(usuario.getNombre());
        usuarioReservaDTO.setEmail(usuario.getEmail());
        usuarioReservaDTO.setTelefono(usuario.getTelefono());

        reservaResponseDTO.setUsuarioReserva(usuarioReservaDTO);
        reservaResponseDTO.setFecha(reserva.getFecha());
        reservaResponseDTO.setIdPublicacion(reserva.getPublicacion().getId());

        return reservaResponseDTO;
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

    public List<ReservaResponseDTO> getReservas(){
        List<Reserva> reservas = reservaRepository.findAll();

        return reservas.stream()
                .map(this::entityReservaToReservaDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public ReservaResponseDTO modificarReserva(ReservaResponseDTO reservaDTO) {

        Reserva reserva = reservaRepository.findById(reservaDTO.getId())
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Solo actualiza si el campo viene en el DTO
        if (reservaDTO.getFecha() != null) {
            reserva.setFecha(reservaDTO.getFecha());
        }

        if (reservaDTO.getMontoReserva() != 0) {
            reserva.setMontoReserva(reservaDTO.getMontoReserva());
        }

        if (reservaDTO.getEstadoReserva() != null) {
            reserva.setEstado(reservaDTO.getEstadoReserva());
        }

        // Modificación de la publicación (solo si viene el ID)
        if (reservaDTO.getIdPublicacion() != null) {
            Publicacion publicacion = publicacionRepository.findById(reservaDTO.getIdPublicacion())
                    .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
            reserva.setPublicacion(publicacion);
        }

        // Actualización de los datos del usuario sin crear uno nuevo
        Usuario usuario = reserva.getUsuario();
        if (reservaDTO.getUsuarioReserva() != null) {
            if (reservaDTO.getUsuarioReserva().getEmail() != null) {
                usuario.setEmail(reservaDTO.getUsuarioReserva().getEmail());
            }
            if (reservaDTO.getUsuarioReserva().getNombre() != null) {
                usuario.setNombre(reservaDTO.getUsuarioReserva().getNombre());
            }
            if (reservaDTO.getUsuarioReserva().getTelefono() != null) {
                usuario.setTelefono(reservaDTO.getUsuarioReserva().getTelefono());
            }
        }

        reserva.setUsuario(usuario);
        reservaRepository.save(reserva);

        return entityReservaToReservaDTO(reserva);
    }

    @Transactional
    public void eliminarReserva(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reservaRepository.delete(reserva);
    }




}
