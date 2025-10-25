package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.repository.PublicacionRepository;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicacionService {
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    // (RF02) Listar mis publicaciones
    public List<Publicacion> getMisPublicaciones(){
        // 1. Obtener el ID del usuario autenticado
        Long userId = obtenerIdUsuarioAutenticado();

        // 2. Llamar al repositorio
        return publicacionRepository.findByVendedorId(userId);
    }

    public Publicacion postPublicacion(Publicacion publicacion){
        Long userId = obtenerIdUsuarioAutenticado();

        Usuario vendedor = usuarioRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        return publicacion;
    }

    private Long obtenerIdUsuarioAutenticado(){
        // TODO: Implementar esto con Spring Security
        // Cuando tengas Spring Security, obtendrás el usuario desde el 'SecurityContextHolder'
        // Por ahora, devolvemos un ID de prueba (ej: 1) para poder probar
        System.out.println("ADVERTENCIA: Usando ID de usuario 'hardcodeado' (1L)");
        return 1L;
    }
}
