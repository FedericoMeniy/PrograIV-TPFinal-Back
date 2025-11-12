package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.dto.LoginUsuarioDTO;
import concesionaria.example.Concesionaria.dto.RegistroUsuarioDto;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.enums.Rol;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
// Implementar UserDetailsService para que Spring Security cargue usuarios
public class UsuarioService implements UserDetailsService {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDto registroUsuarioDto) throws RuntimeException{

        if(usuarioRepository.findByemail(registroUsuarioDto.getEmail()).isPresent()){
            throw new RuntimeException("El email ya esta en uso");
        }
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(registroUsuarioDto.getEmail());
        nuevoUsuario.setNombre(registroUsuarioDto.getNombre());

        // La contraseña se codifica antes de guardar
        nuevoUsuario.setPassword(passwordEncoder.encode(registroUsuarioDto.getPassword()));

        nuevoUsuario.setRol(Rol.USUARIO);

        return usuarioRepository.save(nuevoUsuario);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // CORRECCIÓN CLAVE: Usamos findByemail (minúscula) y eliminamos el cast redundante
        Usuario usuario = usuarioRepository.findByemail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

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
        Usuario responseUsuario = new Usuario();
        responseUsuario.setId(usuarioActualizado.getId());
        responseUsuario.setNombre(usuarioActualizado.getNombre());
        responseUsuario.setEmail(usuarioActualizado.getEmail());
        responseUsuario.setRol(usuarioActualizado.getRol());

        return responseUsuario;
    }
}