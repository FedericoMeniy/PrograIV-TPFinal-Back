package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.entity.Mensaje;
import concesionaria.example.Concesionaria.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @MessageMapping("/chat/{conversacionId}")
    @SendTo("/topic/chat/{conversacionId}")
    public Mensaje enviarMensajePrivado(@DestinationVariable Long conversacionId, Mensaje mensaje) {
        mensaje.setConversacionId(conversacionId);
        return mensajeRepository.save(mensaje);
    }
}