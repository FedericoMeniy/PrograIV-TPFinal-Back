package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.dto.LoginUsuarioDTO;
import concesionaria.example.Concesionaria.dto.PublicacionResponseDTO;
import concesionaria.example.Concesionaria.dto.RegistroUsuarioDTO;
import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.enums.Rol;
import concesionaria.example.Concesionaria.repository.PublicacionRepository;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
// Implementar UserDetailsService para que Spring Security cargue usuarios
public class UsuarioService implements UserDetailsService {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDTO registroUsuarioDto) throws RuntimeException{

        if(usuarioRepository.findByemail(registroUsuarioDto.getEmail()).isPresent()){
            throw new RuntimeException("El email ya esta en uso");
        }

        if(usuarioRepository.existsByTelefono(registroUsuarioDto.getTelefono())){
            throw new RuntimeException("El telefono ya esta registrado.");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(registroUsuarioDto.getEmail());
        nuevoUsuario.setNombre(registroUsuarioDto.getNombre());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroUsuarioDto.getPassword()));
        nuevoUsuario.setTelefono(registroUsuarioDto.getTelefono());

        nuevoUsuario.setRol(Rol.USUARIO);

        return usuarioRepository.save(nuevoUsuario);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByemail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return usuario;
    }

    public Usuario actualizarEmail(Long id, String nuevoEmail) {
        // 1. Buscar al usuario por ID
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // 2. Actualizar el nombre
        usuario.setEmail(nuevoEmail);

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

    @Transactional
    public void toggleFavorito(String emailUsuario, Long idPublicacion) {
        Usuario usuario = usuarioRepository.findByemail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        if (usuario.getFavoritos().contains(publicacion)) {
            usuario.getFavoritos().remove(publicacion); // Si ya es favorito, lo saca (dislike)
        } else {
            usuario.getFavoritos().add(publicacion); // Si no es favorito, lo agrega (like)
        }
        usuarioRepository.save(usuario);
    }

    public List<PublicacionResponseDTO> getFavoritos(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByemail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return usuario.getFavoritos().stream()
                .map(PublicacionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}