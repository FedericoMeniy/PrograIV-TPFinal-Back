package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.dto.LoginUsuarioDTO;
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

    public Usuario login(LoginUsuarioDTO loginDto) {
        // 1. Buscar al usuario por email
        Usuario usuario = usuarioRepository.findByemail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + loginDto.getEmail()));

        // 2. Verificar la contraseña
        if (!passwordEncoder.matches(loginDto.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // 3. Devolver usuario (sin contraseña por seguridad)
        usuario.setPassword(null);
        return usuario;
    }

    public Usuario actualizarNombre(Long id, String nuevoNombre) {
        // 1. Buscar al usuario por ID
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // 2. Actualizar el nombre
        usuario.setNombre(nuevoNombre);

        // 3. Guardar los cambios en la BD
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        // 4. Devolver el usuario actualizado (sin contraseña)
        usuarioActualizado.setPassword(null);
        return usuarioActualizado;
    }
}