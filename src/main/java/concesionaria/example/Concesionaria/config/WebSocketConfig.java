package concesionaria.example.Concesionaria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer; // <-- ¡ESTE ES EL IMPORT CORRECTO!

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { // <-- ¡Y ESTA ES LA INTERFAZ CORRECTA!

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Los canales a los que los clientes se van a suscribir para ESCUCHAR
        config.enableSimpleBroker("/topic");

        // El prefijo para los mensajes que van DESDE el cliente hacia el servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // La URL de conexión inicial (punto de entrada). Permitimos CORS para Angular.
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS();
    }
}