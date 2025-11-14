package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.dto.*;
import concesionaria.example.Concesionaria.entity.*;
import concesionaria.example.Concesionaria.enums.EstadoPublicacion;
import concesionaria.example.Concesionaria.enums.Rol;
import concesionaria.example.Concesionaria.enums.TipoPublicacion;
import concesionaria.example.Concesionaria.repository.*;
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
    private final ReservaRepository reservaRepository;

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

            emailService.sendEmail("pellegrinijulianmauro@gmail.com","Publicacion creada","Tu publicacion en 'MyCar' ha sido realizada, estara pendiente de aceptacion");
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

        // 4. Actualizar solo los campos que vienen en el DTO (actualización parcial)

        // Actualizar descripción si viene en el DTO
        if(dto.getDescripcion() != null && !dto.getDescripcion().trim().isEmpty()){
            publicacionExistente.setDescripcion(dto.getDescripcion().trim());
        }

        // Actualizar auto solo si viene en el DTO
        if(dto.getAuto() != null){
            AutoRequestDTO autoDTO = dto.getAuto();

            // Actualizar campos del auto solo si vienen en el DTO
            if(autoDTO.getMarca() != null && !autoDTO.getMarca().trim().isEmpty()){
                autoExistente.setMarca(autoDTO.getMarca().trim());
            }
            if(autoDTO.getModelo() != null && !autoDTO.getModelo().trim().isEmpty()){
                autoExistente.setModelo(autoDTO.getModelo().trim());
            }
            if(autoDTO.getAnio() != null){
                autoExistente.setAnio(autoDTO.getAnio());
            }
            if(autoDTO.getKm() != null && !autoDTO.getKm().trim().isEmpty()){
                autoExistente.setKm(autoDTO.getKm().trim());
            }
            if(autoDTO.getColor() != null && !autoDTO.getColor().trim().isEmpty()) {
                autoExistente.setColor(autoDTO.getColor().trim());
            }
            if(autoDTO.getPrecio() != 0.0){
                autoExistente.setPrecio(autoDTO.getPrecio());
            }

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

        Publicacion publicacionExistente = publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));

        Usuario vendedor = usuarioRepository.findByemail(emailVendedor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));


        if(!publicacionExistente.getVendedor().getId().equals(vendedor.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta publicacion.");
        }

        Auto auto = publicacionExistente.getAuto();


        FichaTecnica ficha = (auto != null) ? auto.getFichaTecnica() : null;


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

        emailService.sendEmail("pellegrinijulianmauro@gmail.com","Publicacion Aprobada","Tu publicacion en 'MyCar' ha sido aprobada, esperemos puedas vender tu auto pronto!");

        return PublicacionMapper.toResponseDTO(publicacionAprobada);
    }

    @Transactional
    public PublicacionResponseDTO rechazarPublicacion(Long idPublicacion){
        Publicacion publicacion = publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada."));

        if(publicacion.getEstado() != EstadoPublicacion.PENDIENTE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicacion no esta pendiente para ser rechazada");
        }

        publicacion.setEstado(EstadoPublicacion.RECHAZADA);
        Publicacion publicacionRechazada = publicacionRepository.save(publicacion);

        emailService.sendEmail("pellegrinijulianmauro@gmail.com","Publicacion rechazada","Tu publicacion en 'MyCar' ha sido rechazada, por favor revisa todo correctamente antes de enviar, no se aceptaran cosas fuera de lugar");


        return PublicacionMapper.toResponseDTO(publicacionRechazada);
    }


    private Usuario findVendedorByEmail(String emailVendedor) {
        return usuarioRepository.findByemail(emailVendedor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));
    }

    @Transactional
    public void marcarComoVendidaYEliminar(Long idPublicacion, String emailVendedor){

        deletePublicacion(idPublicacion, emailVendedor);
    }

    public PublicacionEstadisticasDTO getEstadisticasPublicaciones() {
        long total = publicacionRepository.count();
        long pendientes = publicacionRepository.countByEstado(EstadoPublicacion.PENDIENTE);
        long aceptadas = publicacionRepository.countByEstado(EstadoPublicacion.ACEPTADA);
        long rechazadas = publicacionRepository.countByEstado(EstadoPublicacion.RECHAZADA);
        long porUsuario = publicacionRepository.countByTipoPublicacion(TipoPublicacion.USUARIO);
        long porConcesionaria = publicacionRepository.countByTipoPublicacion(TipoPublicacion.CONCESIONARIA);

        return PublicacionEstadisticasDTO.builder()
                .totalPublicaciones(total)
                .pendientes(pendientes)
                .aceptadas(aceptadas)
                .rechazadas(rechazadas)
                .usuario(porUsuario)
                .concesionaria(porConcesionaria)
                .build();
    }

    @Transactional
    public void deletePublicacionAdmin(Long idPublicacion){
        Publicacion publicacionExistente = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));

        // PRIMERO: Eliminar todas las reservas asociadas a esta publicación
        List<Reserva> reservas = reservaRepository.findByPublicacion_Id(idPublicacion);
        if (reservas != null && !reservas.isEmpty()) {
            reservaRepository.deleteAll(reservas);
        }

        // SEGUNDO: Eliminar la publicación y sus entidades relacionadas
        Auto auto = publicacionExistente.getAuto();
        FichaTecnica ficha = (auto != null) ? auto.getFichaTecnica() : null;

        publicacionRepository.delete(publicacionExistente);

        if(auto != null){
            autoRepository.delete(auto);
        }

        if(ficha != null){
            fichaTecnicaRepository.delete(ficha);
        }
    }
}
