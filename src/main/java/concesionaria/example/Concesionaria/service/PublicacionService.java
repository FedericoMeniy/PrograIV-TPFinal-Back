package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.dto.AutoRequestDTO;
import concesionaria.example.Concesionaria.dto.FichaTecnicaRequestDTO;
import concesionaria.example.Concesionaria.dto.PublicacionRequestDTO;
import concesionaria.example.Concesionaria.dto.PublicacionResponseDTO;
import concesionaria.example.Concesionaria.entity.Auto;
import concesionaria.example.Concesionaria.entity.FichaTecnica;
import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.enums.EstadoPublicacion;
import concesionaria.example.Concesionaria.enums.Rol;
import concesionaria.example.Concesionaria.enums.TipoPublicacion;
import concesionaria.example.Concesionaria.repository.AutoRepository;
import concesionaria.example.Concesionaria.repository.FichaTecnicaRepository;
import concesionaria.example.Concesionaria.repository.PublicacionRepository;
import concesionaria.example.Concesionaria.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicacionService {
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AutoRepository autoRepository;
    private final FichaTecnicaRepository fichaTecnicaRepository;

    public List<PublicacionResponseDTO> getPublicacion(Long idUsuario){
        List<Publicacion> publicaciones = publicacionRepository.findByVendedorId(idUsuario);
        return PublicacionMapper.toResponseDTOList(publicaciones);
    }

    @Transactional
    public PublicacionResponseDTO postPublicacion(PublicacionRequestDTO dto, Long idUsuario){
        // 1. Encontrar al usuario vendedor
        Usuario vendedor = usuarioRepository.findById(idUsuario).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 1. Mapear FichaTecnica DTO a Entidad
        FichaTecnicaRequestDTO fichaDTO = dto.getAuto().getFichaTecnica();
        FichaTecnica fichaTecnica = new FichaTecnica();
        fichaTecnica.setMotor(fichaDTO.getMotor());
        fichaTecnica.setCombustible(fichaDTO.getCombustible());
        fichaTecnica.setCaja(fichaDTO.getCaja());
        fichaTecnica.setPuertas(fichaDTO.getPuertas());
        fichaTecnica.setPotencia(fichaDTO.getPotencia());
        FichaTecnica fichaGuardada = fichaTecnicaRepository.save(fichaTecnica);

        // 2. Mapear Auto DTO a Entidad
        AutoRequestDTO autoDTO = dto.getAuto();
        Auto auto = new Auto();
        auto.setNombre(autoDTO.getNombre());
        auto.setModelo(autoDTO.getModelo());
        auto.setPrecio(autoDTO.getPrecio());
        auto.setAnio(autoDTO.getAnio());
        auto.setKm(autoDTO.getKm());
        auto.setColor(autoDTO.getColor());
        auto.setFichaTecnica(fichaGuardada); // Asignamos la ficha ya guardada
        Auto autoGuardado = autoRepository.save(auto);

        // 3. Mapear Publicacion DTO a Entidad
        Publicacion publicacion = new Publicacion();
        publicacion.setDescripcion(dto.getDescripcion());
        publicacion.setAuto(autoGuardado); // Asignamos el auto ya guardado
        publicacion.setVendedor(vendedor); // Asignamos el vendedor

        // 4. Asignar estados por defecto
        publicacion.setEstado(EstadoPublicacion.PENDIENTE);
        publicacion.setTipoPublicacion(
                vendedor.getRol() == Rol.ADMIN ? TipoPublicacion.CONCESIONARIA : TipoPublicacion.USUARIO
        );

        Publicacion publicacionGuardada = publicacionRepository.save(publicacion);

        // 5. Convertir la Entidad guardada a DTO de respuesta
        return PublicacionMapper.toResponseDTO(publicacionGuardada);
    }

    @Transactional
    public PublicacionResponseDTO putPublicacion(Long idPublicacion, PublicacionRequestDTO dto, Long idUsuario){
        // 1. Buscar la publicación existente
        Publicacion publicacionExistente = publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));

        if(!publicacionExistente.getVendedor().getId().equals(idUsuario)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar esta publicacion");
        }

        Auto autoExistente = publicacionExistente.getAuto();
        FichaTecnica fichaExistente = autoExistente.getFichaTecnica();

        AutoRequestDTO autoDTO = dto.getAuto();
        FichaTecnicaRequestDTO fichaDTO = autoDTO.getFichaTecnica();

        publicacionExistente.setDescripcion(dto.getDescripcion());

        autoExistente.setNombre(autoDTO.getNombre());
        autoExistente.setModelo(autoDTO.getModelo());
        autoExistente.setPrecio(autoDTO.getPrecio());
        autoExistente.setAnio(autoDTO.getAnio());
        autoExistente.setKm(autoDTO.getKm());
        autoExistente.setColor(autoDTO.getColor());

        fichaExistente.setMotor(fichaDTO.getMotor());
        fichaExistente.setCombustible(fichaDTO.getCombustible());
        fichaExistente.setCaja(fichaDTO.getCaja());
        fichaExistente.setPuertas(fichaDTO.getPuertas());
        fichaExistente.setPotencia(fichaDTO.getPotencia());

        fichaTecnicaRepository.save(fichaExistente);
        autoRepository.save(autoExistente);
        Publicacion publicacionGuardada = publicacionRepository.save(publicacionExistente);

        return PublicacionMapper.toResponseDTO(publicacionGuardada);
    }

    @Transactional
    public void deletePublicacion(Long idPublicacion, Long idUsuario){
        // 1. Buscar la publicación existente
        Publicacion publicacionExistente = publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));

        // 2. *** VERIFICACIÓN DE PROPIEDAD ***
        if(!publicacionExistente.getVendedor().getId().equals(idUsuario)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta publicacion.");
        }

        // 3. Obtener el Auto y FichaTecnica asociados
        Auto auto = publicacionExistente.getAuto();

        // (Manejo defensivo por si la FichaTecnica es nula en la BD)
        FichaTecnica ficha = (auto != null) ? auto.getFichaTecnica() : null;

        // 4. Eliminar en orden
        publicacionRepository.delete(publicacionExistente);

        if(auto != null){
            autoRepository.delete(auto);
        }
        if(ficha != null){
            fichaTecnicaRepository.delete(ficha);
        }
    }
}
