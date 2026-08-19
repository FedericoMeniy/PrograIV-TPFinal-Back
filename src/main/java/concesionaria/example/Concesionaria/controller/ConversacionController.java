package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.ConversacionResumenDTO;
import concesionaria.example.Concesionaria.entity.Conversacion;
import concesionaria.example.Concesionaria.entity.Mensaje;
import concesionaria.example.Concesionaria.repository.ConversacionRepository;
import concesionaria.example.Concesionaria.repository.MensajeRepository;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversacion")
public class ConversacionController {

    @Autowired
    private ConversacionRepository conversacionRepository;

    @Autowired
    private MensajeRepository mensajeRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/iniciar")
    public ResponseEntity<?> iniciarOObtenerChat(
            @RequestParam Long publicacionId,
            @RequestParam String compradorEmail,
            @RequestParam String vendedorEmail) {

        Conversacion conversacion = conversacionRepository
                .findByPublicacionIdAndCompradorEmail(publicacionId, compradorEmail)
                .orElseGet(() -> {
                    Conversacion nueva = new Conversacion();
                    nueva.setPublicacionId(publicacionId);
                    nueva.setCompradorEmail(compradorEmail);
                    nueva.setVendedorEmail(vendedorEmail);
                    return conversacionRepository.save(nueva);
                });

        List<Mensaje> historial = mensajeRepository.findByConversacionIdOrderByFechaAsc(conversacion.getId());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("conversacionId", conversacion.getId());
        respuesta.put("mensajes", historial);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/mis-chats")
    public ResponseEntity<List<ConversacionResumenDTO>> obtenerMisChats(@RequestParam String emailUsuario) {

        List<Conversacion> misConversaciones = conversacionRepository
                .findByCompradorEmailOrVendedorEmail(emailUsuario, emailUsuario);

        List<ConversacionResumenDTO> resumen = misConversaciones.stream().map(conv -> {
            ConversacionResumenDTO dto = new ConversacionResumenDTO();
            dto.setConversacionId(conv.getId());
            dto.setPublicacionId(conv.getPublicacionId());

            String emailDelOtro;

            if (conv.getCompradorEmail().equals(emailUsuario)) {
                emailDelOtro = conv.getVendedorEmail();
                dto.setRol("COMPRADOR");
            } else {
                emailDelOtro = conv.getCompradorEmail();
                dto.setRol("VENDEDOR");
            }

            dto.setEmailContacto(emailDelOtro);

            usuarioRepository.findByemail(emailDelOtro).ifPresent(usuario -> {
                dto.setNombreContacto(usuario.getNombre());
            });

            if (dto.getNombreContacto() == null || dto.getNombreContacto().isEmpty()) {
                dto.setNombreContacto(emailDelOtro);
            }

            mensajeRepository.findTopByConversacionIdOrderByFechaDesc(conv.getId())
                    .ifPresent(ultimo -> {
                        dto.setUltimoMensaje(ultimo.getContenido());
                        dto.setFechaUltimoMensaje(ultimo.getFecha());
                    });

            return dto;
        }).toList();

        List<ConversacionResumenDTO> resumenOrdenado = new java.util.ArrayList<>(resumen);
        resumenOrdenado.sort((a, b) -> {
            if (a.getFechaUltimoMensaje() == null) return 1;
            if (b.getFechaUltimoMensaje() == null) return -1;
            return b.getFechaUltimoMensaje().compareTo(a.getFechaUltimoMensaje());
        });

        return ResponseEntity.ok(resumenOrdenado);
    }

    @GetMapping("/no-leidos")
    public ResponseEntity<Long> contarNoLeidos(@RequestParam String email) {
        long cantidad = mensajeRepository.contarMensajesNoLeidos(email);
        return ResponseEntity.ok(cantidad);
    }

    @Transactional
    @PostMapping("/marcar-leidos")
    public ResponseEntity<?> marcarLeidos(@RequestParam Long conversacionId, @RequestParam String email) {
        mensajeRepository.marcarComoLeidos(conversacionId, email);
        return ResponseEntity.ok().build();
    }
}
