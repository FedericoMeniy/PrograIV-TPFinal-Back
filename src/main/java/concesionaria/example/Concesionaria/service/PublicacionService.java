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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile; // <-- NUEVO IMPORT
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList; // <-- NUEVO IMPORT
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    // Inyectado a través de @RequiredArgsConstructor (o @Autowired si quitas final)
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AutoRepository autoRepository;
    private final FichaTecnicaRepository fichaTecnicaRepository;
    private final EmailService emailService;

    // Inyectado explícitamente (Asegúrate que ImageStorageService esté anotado con @Service)
    @Autowired
    private ImageStorageService imageStorageService;

    public List<PublicacionResponseDTO> getPublicacion(String emailVendedor){
        Usuario vendedor = usuarioRepository.findByemail(emailVendedor).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no valido"));
        List<Publicacion> publicaciones = publicacionRepository.findByVendedorId(vendedor.getId());
        return PublicacionMapper.toResponseDTOList(publicaciones);
    }

    public List<PublicacionResponseDTO> getCatalogoTienda(){
        List<Publicacion> publicaciones = publicacionRepository.findByEstadoAndTipoPublicacion(EstadoPublicacion.ACEPTADA, TipoPublicacion.CONCESIONARIA);
        return PublicacionMapper.toResponseDTOList(publicaciones);
    }

    public List<PublicacionResponseDTO> getCatalogoUsados(){
        List<Publicacion> publicaciones = publicacionRepository.findByEstadoAndTipoPublicacion(EstadoPublicacion.ACEPTADA, TipoPublicacion.USUARIO);
        return PublicacionMapper.toResponseDTOList(publicaciones);
    }

    public PublicacionResponseDTO getPublicacionById(Long idPublicacion){
        Publicacion publicacion = publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));
        return PublicacionMapper.toResponseDTO(publicacion);
    }

    @Transactional
    public PublicacionResponseDTO postPublicacion(PublicacionRequestDTO dto, List<MultipartFile> files, String emailVendedor){
        // 1. Encontrar al usuario vendedor
        Usuario vendedor = usuarioRepository.findByemail(emailVendedor).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 2. Guardar las imágenes y obtener sus URLs
        List<String> imageUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = imageStorageService.store(file);
                    imageUrls.add(url);
                }
            }
        }

        // 3. Mapear FichaTecnica DTO a Entidad
        FichaTecnicaRequestDTO fichaDTO = dto.getAuto().getFichaTecnica();
        FichaTecnica fichaTecnica = new FichaTecnica();
        fichaTecnica.setMotor(fichaDTO.getMotor());
        fichaTecnica.setCombustible(fichaDTO.getCombustible());
        fichaTecnica.setCaja(fichaDTO.getCaja());
        fichaTecnica.setPuertas(fichaDTO.getPuertas());
        fichaTecnica.setPotencia(fichaDTO.getPotencia());
        FichaTecnica fichaGuardada = fichaTecnicaRepository.save(fichaTecnica);

        // 4. Mapear Auto DTO a Entidad
        AutoRequestDTO autoDTO = dto.getAuto();
        Auto auto = new Auto();
        auto.setMarca(autoDTO.getMarca());
        auto.setModelo(autoDTO.getModelo());
        auto.setPrecio(autoDTO.getPrecio());
        auto.setAnio(autoDTO.getAnio());
        auto.setKm(autoDTO.getKm());
        auto.setColor(autoDTO.getColor());
        auto.setFichaTecnica(fichaGuardada); // Asignamos la ficha ya guardada

        // --- ASIGNAR IMÁGENES AL AUTO ---
        auto.setImagenesUrl(imageUrls);

        Auto autoGuardado = autoRepository.save(auto);

        // 3. Mapear Publicacion DTO a Entidad
        Publicacion publicacion = new Publicacion();
        publicacion.setDescripcion(dto.getDescripcion());
        publicacion.setAuto(autoGuardado); // Asignamos el auto ya guardado
        publicacion.setVendedor(vendedor); // Asignamos el vendedor

        // 4. Asignar estados por defecto
        if(vendedor.getRol() == Rol.ADMIN){
            publicacion.setEstado(EstadoPublicacion.ACEPTADA);
            publicacion.setTipoPublicacion(TipoPublicacion.CONCESIONARIA);

        }else{
            publicacion.setEstado(EstadoPublicacion.PENDIENTE);
            publicacion.setTipoPublicacion(TipoPublicacion.USUARIO);

            emailService.sendEmail("nahuelacuna426@gmail.com","Publicacion creada","Tu publicacion en 'MyCar' ha sido realizada, esperamos puedas vender tu auto pronto!");
        }

        Publicacion publicacionGuardada = publicacionRepository.save(publicacion);

        // 5. Convertir la Entidad guardada a DTO de respuesta
        return PublicacionMapper.toResponseDTO(publicacionGuardada);
    }

    @Transactional
    public PublicacionResponseDTO putPublicacion(Long idPublicacion, PublicacionRequestDTO dto, String emailVendedor){
        // 1. Buscar la publicación existente
        Publicacion publicacionExistente = publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));

        Usuario vendedor = usuarioRepository.findByemail(emailVendedor).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no valido."));
        if(!publicacionExistente.getVendedor().getId().equals(vendedor.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar esta publicacion");
        }

        Auto autoExistente = publicacionExistente.getAuto();
        FichaTecnica fichaExistente = autoExistente.getFichaTecnica();

        AutoRequestDTO autoDTO = dto.getAuto();
        FichaTecnicaRequestDTO fichaDTO = autoDTO.getFichaTecnica();

        publicacionExistente.setDescripcion(dto.getDescripcion());

        autoExistente.setMarca(autoDTO.getMarca());
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
    public void deletePublicacion(Long idPublicacion, String emailVendedor){
        // 1. Buscar la publicación existente
        Publicacion publicacionExistente = publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));

        Usuario vendedor = usuarioRepository.findByemail(emailVendedor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));

        // 2. *** VERIFICACIÓN DE PROPIEDAD ***
        if(!publicacionExistente.getVendedor().getId().equals(vendedor.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta publicacion.");
        }

        // (Nota: Aquí también deberías eliminar las imágenes del disco
        // usando imageStorageService.delete(url) si implementas ese método)

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

    public List<PublicacionResponseDTO> getPublicacionesPendientes(){
        List<Publicacion> publicaciones = publicacionRepository.findByEstado(EstadoPublicacion.PENDIENTE);
        return PublicacionMapper.toResponseDTOList(publicaciones);
    }

    @Transactional
    public PublicacionResponseDTO aprobarPublicacion(Long idPublicacion){
        Publicacion publicacion = publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada."));

        if(publicacion.getEstado() != EstadoPublicacion.PENDIENTE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicación no está pendiente de aprobación");
        }

        publicacion.setEstado(EstadoPublicacion.ACEPTADA);
        Publicacion publicacionAprobada = publicacionRepository.save(publicacion);

        return PublicacionMapper.toResponseDTO(publicacionAprobada);
    }

    @Transactional // <-- Añadido @Transactional
    public PublicacionResponseDTO rechazarPublicacion(Long idPublicacion){
        Publicacion publicacion = publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada."));

        if(publicacion.getEstado() != EstadoPublicacion.PENDIENTE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicacion no esta pendiente para ser rechazada");
        }

        publicacion.setEstado(EstadoPublicacion.RECHAZADA);
        Publicacion publicacionRechazada = publicacionRepository.save(publicacion);

        return PublicacionMapper.toResponseDTO(publicacionRechazada);
    }

    // Corregí un error de tipeo en el 'findByemail' que tenías
    private Usuario findVendedorByEmail(String emailVendedor) {
        return usuarioRepository.findByemail(emailVendedor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));
    }
}
