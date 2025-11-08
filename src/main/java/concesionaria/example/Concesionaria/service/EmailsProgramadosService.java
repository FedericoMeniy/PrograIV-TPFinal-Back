package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
public class EmailsProgramadosService {

    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;

    @Scheduled(cron = "0 30 12 ? * FRI")
    public void mailFinanciacion(){

        List<Usuario> usuarios = usuarioRepository.findAll();

        String asunto = "Mycar financiaciones";
        String contenido = "Queremos informarte sobre nuestros nuevos planes de pago y financiaciones "
                + "disponibles para tu próximo vehículo. ¡Las mejores cuotas te esperan!\n\n"
                + "Visítanos o responde a este correo para más detalles.\n\n"
                + "Saludos cordiales,\n"
                + "Tu equipo de Concesionaria MyCar.";

        for(Usuario usuario : usuarios){
            emailService.sendEmail(usuario.getEmail(),asunto,"Hola, "+ usuario.getNombre()+ "\n\n" + contenido);
        }
    }
}
