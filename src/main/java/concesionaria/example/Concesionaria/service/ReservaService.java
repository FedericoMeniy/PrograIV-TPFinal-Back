package concesionaria.example.Concesionaria.service;

import com.mercadopago.resources.payment.Payment;
import concesionaria.example.Concesionaria.dto.PublicacionResponseDTO;
import concesionaria.example.Concesionaria.dto.ReservaRequestDTO;
import concesionaria.example.Concesionaria.dto.ReservaResponseDTO;
import concesionaria.example.Concesionaria.dto.UsuarioReservaDTO;
import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.entity.Reserva;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.enums.EstadoReserva;
import concesionaria.example.Concesionaria.repository.PublicacionRepository;
import concesionaria.example.Concesionaria.repository.ReservaRepository;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @Transactional
    public String iniciarReserva(ReservaRequestDTO reservaRequestDTO) {
        Publicacion publicacion = publicacionRepository.findById(reservaRequestDTO.getIdPublicacion())
                .orElseThrow(() -> new RuntimeException("La publicación no existe"));

        UsuarioReservaDTO usuarioDTO = reservaRequestDTO.getUsuarioReservaDTO();
        Usuario usuarioExistente = usuarioRepository.findByemail(usuarioDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));

        List<Reserva> reservasPagadas = reservaRepository.findByPublicacionIdAndEstado(
                publicacion.getId(), EstadoReserva.ACEPTADA);

        if (!reservasPagadas.isEmpty()) {
            throw new RuntimeException("Este vehículo ya fue reservado exitosamente y no admite nuevos pagos.");
        }

        List<Reserva> reservasPendientes = reservaRepository.findByPublicacionIdAndEstado(
                publicacion.getId(), EstadoReserva.PENDIENTE);

        Reserva nuevaReserva = null;

        for (Reserva existente : reservasPendientes) {

            if (existente.getUsuario().getId().equals(usuarioExistente.getId())) {
                nuevaReserva = existente;
                nuevaReserva.setFecha(LocalDateTime.now());
                break;
            } else {
                long minutosTranscurridos = java.time.temporal.ChronoUnit.MINUTES.between(existente.getFecha(), LocalDateTime.now());

                if (minutosTranscurridos > 10) {
                    existente.setEstado(EstadoReserva.CANCELADA);
                    reservaRepository.save(existente);
                } else {
                    long minutosRestantes = 10 - minutosTranscurridos;
                    throw new RuntimeException("El vehículo está siendo reservado por otro cliente. Por favor, intentá de nuevo en " + minutosRestantes + " minutos.");
                }
            }
        }

        if (nuevaReserva == null) {
            nuevaReserva = new Reserva();
            nuevaReserva.setUsuario(usuarioExistente);
            nuevaReserva.setPublicacion(publicacion);
            nuevaReserva.setEstado(EstadoReserva.PENDIENTE);
            nuevaReserva.setFecha(LocalDateTime.now());
            nuevaReserva.setMontoReserva(publicacion.getAuto().getPrecio() * 0.10);
        }

        Reserva reservaPreGuardada = reservaRepository.save(nuevaReserva);

        String asuntoPendiente = "Tu reserva está pendiente de pago - MyCar";
        String mensajePendiente = "Hola " + usuarioExistente.getNombre() + ",\n\n" +
                "Iniciaste el proceso de reserva para el vehículo " + publicacion.getAuto().getMarca() + " " + publicacion.getAuto().getModelo() + ".\n" +
                "El estado actual es PENDIENTE. Recordá que tenés 10 minutos para completar el pago de $" + nuevaReserva.getMontoReserva() + " y asegurar el vehículo.\n\n" +
                "Saludos, el equipo de MyCar.";

        emailService.sendEmail(usuarioExistente.getEmail(), asuntoPendiente, mensajePendiente);

        String pagoURL = mercadoPagoService.crearPreferenciaDePago(
                reservaPreGuardada.getPublicacion(),
                reservaPreGuardada.getId(),
                reservaPreGuardada.getMontoReserva()
        );

        return pagoURL;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void limpiarReservasVencidas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(2);

        List<Reserva> reservasVencidas = reservaRepository.findByEstadoAndFechaBefore(
                EstadoReserva.PENDIENTE, limite
        );

        for (Reserva reserva : reservasVencidas) {
            reserva.setEstado(EstadoReserva.CANCELADA);
            reservaRepository.save(reserva);
            System.out.println("El tiempo expiró. Reserva ID " + reserva.getId() + " cancelada automáticamente.");
        }
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

        reservaResponseDTO.setId(reserva.getId());
        reservaResponseDTO.setEstadoReserva(reserva.getEstado());
        reservaResponseDTO.setMontoReserva(reserva.getMontoReserva());

        usuarioReservaDTO.setNombre(usuario.getNombre());
        usuarioReservaDTO.setEmail(usuario.getEmail());
        usuarioReservaDTO.setTelefono(usuario.getTelefono());

        reservaResponseDTO.setUsuarioReserva(usuarioReservaDTO);
        reservaResponseDTO.setFecha(reserva.getFecha());

        PublicacionResponseDTO pubDto = PublicacionMapper.toResponseDTO(reserva.getPublicacion());
        reservaResponseDTO.setPublicacion(pubDto);

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
            Payment payment = mercadoPagoService.obtenerDetallesDePago(paymentId);
            String estadoMP = payment.getStatus().toString();

            System.out.println("Pago ID: " + paymentId + " - Estado de MP: " + estadoMP);

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

        EstadoReserva estadoAnterior = reserva.getEstado();

        if (reservaDTO.getFecha() != null) {
            reserva.setFecha(reservaDTO.getFecha());
        }

        if (reservaDTO.getMontoReserva() != 0) {
            reserva.setMontoReserva(reservaDTO.getMontoReserva());
        }

        if (reservaDTO.getEstadoReserva() != null) {
            reserva.setEstado(reservaDTO.getEstadoReserva());
        }

        if (reservaDTO.getPublicacion() != null && reservaDTO.getPublicacion().getId() != null) {
            Publicacion publicacion = publicacionRepository.findById(reservaDTO.getPublicacion().getId())
                    .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
            reserva.setPublicacion(publicacion);
        }

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

        if (reservaDTO.getEstadoReserva() != null && !estadoAnterior.equals(reservaDTO.getEstadoReserva())) {
            String nombreAuto = reserva.getPublicacion().getAuto().getMarca() + " " + reserva.getPublicacion().getAuto().getModelo();
            String asunto = "";
            String msjAdmin = "";

            if (reservaDTO.getEstadoReserva() == EstadoReserva.ACEPTADA) {
                asunto = "Reserva Aceptada - MyCar";
                msjAdmin = "Hola " + usuario.getNombre() + ",\n\n¡Buenas noticias! Un administrador ha ACEPTADO tu reserva para el vehículo " + nombreAuto + ".\nNos pondremos en contacto pronto para continuar con el proceso.";
            } else if (reservaDTO.getEstadoReserva() == EstadoReserva.CANCELADA) {
                asunto = "Reserva Cancelada - MyCar";
                msjAdmin = "Hola " + usuario.getNombre() + ",\n\nTe informamos que un administrador ha CANCELADO tu reserva para el vehículo " + nombreAuto + ".\nAnte cualquier duda, por favor contactate con nosotros.";
            }

            if (!asunto.isEmpty()) {
                emailService.sendEmail(usuario.getEmail(), asunto, msjAdmin);
            }
        }

        return entityReservaToReservaDTO(reserva);
    }

    @Transactional
    public void eliminarReserva(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reservaRepository.delete(reserva);
    }

}