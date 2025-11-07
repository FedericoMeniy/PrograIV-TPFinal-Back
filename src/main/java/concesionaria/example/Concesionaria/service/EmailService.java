package concesionaria.example.Concesionaria.service;

import lombok.Data;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Data
public class EmailService {

    private final JavaMailSender javaMailSender;


    public void sendEmail(String to, String subject, String content){

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom("mycarautos.mdp@gmail.com");
        javaMailSender.send(message);
    }
}
