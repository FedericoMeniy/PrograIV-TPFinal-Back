package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.dto.RegistroUsuarioDto;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.enums.Rol;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;

    // --- ESTE ES EL CONSTRUCTOR AÑADIDO ---
    // Inyecta las dependencias (el Repositorio y el Encoder definido en SecurityConfig)
    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    // ----------------------------------------

    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDto registroUsuarioDto) throws RuntimeException{

        if(usuarioRepository.findByemail(registroUsuarioDto.getEmail()).isPresent()){
            throw new RuntimeException("El email ya esta en uso");
        }
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(registroUsuarioDto.getEmail());
        nuevoUsuario.setNombre(registroUsuarioDto.getNombre());

        // Esto ahora funciona porque passwordEncoder fue inyectado
        nuevoUsuario.setPassword(passwordEncoder.encode(registroUsuarioDto.getPassword()));

        nuevoUsuario.setRol(Rol.USUARIO);


        // Esto ahora funciona porque usuarioRepository fue inyectado
        return usuarioRepository.save(nuevoUsuario);
    }
}