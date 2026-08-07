package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.ConversacionResumenDTO;
import concesionaria.example.Concesionaria.entity.Conversacion;
import concesionaria.example.Concesionaria.entity.Mensaje;
import concesionaria.example.Concesionaria.repository.ConversacionRepository;
import concesionaria.example.Concesionaria.repository.MensajeRepository;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
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

        // 1. Buscamos si ya hay una conversación
        Conversacion conversacion = conversacionRepository
                .findByPublicacionIdAndCompradorEmail(publicacionId, compradorEmail)
                .orElseGet(() -> {
                    // Si no existe, la creamos vacía
                    Conversacion nueva = new Conversacion();
                    nueva.setPublicacionId(publicacionId);
                    nueva.setCompradorEmail(compradorEmail);
                    nueva.setVendedorEmail(vendedorEmail);
                    return conversacionRepository.save(nueva);
                });

        // 2. Buscamos el historial de mensajes de esa conversación
        List<Mensaje> historial = mensajeRepository.findByConversacionIdOrderByFechaAsc(conversacion.getId());

        // 3. Armamos la respuesta para Angular
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("conversacionId", conversacion.getId());
        respuesta.put("mensajes", historial);

        return ResponseEntity.ok(respuesta);
    }
    @GetMapping("/mis-chats")
    public ResponseEntity<List<ConversacionResumenDTO>> obtenerMisChats(@RequestParam String emailUsuario) {

        // 1. Buscamos todas las conversaciones de este usuario
        List<Conversacion> misConversaciones = conversacionRepository
                .findByCompradorEmailOrVendedorEmail(emailUsuario, emailUsuario);

        // 2. Las transformamos al DTO resumido
        List<ConversacionResumenDTO> resumen = misConversaciones.stream().map(conv -> {
            ConversacionResumenDTO dto = new ConversacionResumenDTO();
            dto.setConversacionId(conv.getId());
            dto.setPublicacionId(conv.getPublicacionId());

            String emailDelOtro;

            // Determinamos quién es la otra persona
            if (conv.getCompradorEmail().equals(emailUsuario)) {
                emailDelOtro = conv.getVendedorEmail();
                dto.setRol("COMPRADOR");
            } else {
                emailDelOtro = conv.getCompradorEmail();
                dto.setRol("VENDEDOR");
            }

            dto.setEmailContacto(emailDelOtro);

            // 🔥 Buscamos el nombre del usuario en la base de datos
            usuarioRepository.findByemail(emailDelOtro).ifPresent(usuario -> {
                dto.setNombreContacto(usuario.getNombre());
            });

            // Si por algún motivo no tiene nombre guardado, mostramos el email como plan B
            if (dto.getNombreContacto() == null || dto.getNombreContacto().isEmpty()) {
                dto.setNombreContacto(emailDelOtro);
            }

            // Buscamos el último mensaje
            mensajeRepository.findTopByConversacionIdOrderByFechaDesc(conv.getId())
                    .ifPresent(ultimo -> {
                        dto.setUltimoMensaje(ultimo.getContenido());
                        dto.setFechaUltimoMensaje(ultimo.getFecha());
                    });

            return dto;
        }).toList();

        // 4. Ordenamos la lista para que los chats con actividad reciente queden arriba
        List<ConversacionResumenDTO> resumenOrdenado = new java.util.ArrayList<>(resumen);
        resumenOrdenado.sort((a, b) -> {
            if (a.getFechaUltimoMensaje() == null) return 1;
            if (b.getFechaUltimoMensaje() == null) return -1;
            return b.getFechaUltimoMensaje().compareTo(a.getFechaUltimoMensaje());
        });

        return ResponseEntity.ok(resumenOrdenado);
    }
}