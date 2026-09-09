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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicacionService {
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AutoRepository autoRepository;
    private final FichaTecnicaRepository fichaTecnicaRepository;
    private final EmailService emailService;
    private final ReservaRepository reservaRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    public List<PublicacionResponseDTO> getPublicacion(String emailVendedor){
        Usuario vendedor = usuarioRepository.findByemail(emailVendedor).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no valido"));
        List<Publicacion> publicaciones = publicacionRepository.findByVendedorId(vendedor.getId());
        return PublicacionMapper.toResponseDTOList(publicaciones);
    }

    public List<PublicacionResponseDTO> getCatalogoTienda(){
        List<Publicacion> publicaciones = publicacionRepository.findPublicacionesDisponibles(EstadoPublicacion.ACEPTADA, TipoPublicacion.CONCESIONARIA);
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
        Usuario vendedor = usuarioRepository.findByemail(emailVendedor).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        List<String> imageUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = imageStorageService.store(file);
                    imageUrls.add(url);
                }
            }
        }

        FichaTecnicaRequestDTO fichaDTO = dto.getAuto().getFichaTecnica();
        FichaTecnica fichaTecnica = new FichaTecnica();
        fichaTecnica.setMotor(fichaDTO.getMotor());
        fichaTecnica.setCombustible(fichaDTO.getCombustible());
        fichaTecnica.setCaja(fichaDTO.getCaja());
        fichaTecnica.setPuertas(fichaDTO.getPuertas());
        fichaTecnica.setPotencia(fichaDTO.getPotencia());
        FichaTecnica fichaGuardada = fichaTecnicaRepository.save(fichaTecnica);

        AutoRequestDTO autoDTO = dto.getAuto();
        Auto auto = new Auto();
        auto.setMarca(autoDTO.getMarca());
        auto.setModelo(autoDTO.getModelo());
        auto.setPrecio(autoDTO.getPrecio());
        auto.setAnio(autoDTO.getAnio());
        auto.setKm(autoDTO.getKm());
        auto.setColor(autoDTO.getColor());
        auto.setFichaTecnica(fichaGuardada);
        auto.setImagenesUrl(imageUrls);

        Auto autoGuardado = autoRepository.save(auto);

        Publicacion publicacion = new Publicacion();
        publicacion.setDescripcion(dto.getDescripcion());
        publicacion.setAuto(autoGuardado);
        publicacion.setVendedor(vendedor);

        if(vendedor.getRol() == Rol.ADMIN){
            publicacion.setEstado(EstadoPublicacion.ACEPTADA);
            publicacion.setTipoPublicacion(TipoPublicacion.CONCESIONARIA);
        }else{
            publicacion.setEstado(EstadoPublicacion.PENDIENTE);
            publicacion.setTipoPublicacion(TipoPublicacion.USUARIO);

            emailService.sendEmail(vendedor.getEmail(),"Publicación creada","Tu publicación en 'MyCar' ha sido realizada, estará pendiente de aceptación.");
        }

        Publicacion publicacionGuardada = publicacionRepository.save(publicacion);

        return PublicacionMapper.toResponseDTO(publicacionGuardada);
    }

    @Transactional
    public PublicacionResponseDTO putPublicacion(Long idPublicacion, PublicacionRequestDTO dto, List<MultipartFile> files, String emailVendedor){
        Publicacion publicacionExistente = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));

        Usuario vendedor = usuarioRepository.findByemail(emailVendedor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no valido."));

        Usuario vendedorPublicacion = publicacionExistente.getVendedor();

        if(vendedorPublicacion == null){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "La publicacion no tiene vendedor asignado");
        }

        Long idVendedorPublicacion = vendedorPublicacion.getId();
        Long idVendedorActual = vendedor.getId();

        boolean mismoVendedor = idVendedorPublicacion != null && idVendedorActual != null
                && idVendedorPublicacion.equals(idVendedorActual);

        boolean mismoEmail = vendedorPublicacion.getEmail() != null
                && vendedor.getEmail() != null
                && vendedorPublicacion.getEmail().equalsIgnoreCase(vendedor.getEmail());

        if(!mismoVendedor && !mismoEmail){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar esta publicacion. Vendedor publicacion: " +
                            (vendedorPublicacion.getEmail() != null ? vendedorPublicacion.getEmail() : "null") +
                            ", Vendedor actual: " + (vendedor.getEmail() != null ? vendedor.getEmail() : "null"));
        }

        if(vendedor.getRol() == Rol.USUARIO && (publicacionExistente.getEstado() == EstadoPublicacion.ACEPTADA || publicacionExistente.getEstado() == EstadoPublicacion.RECHAZADA)){
            publicacionExistente.setEstado(EstadoPublicacion.PENDIENTE);
        }

        Auto autoExistente = publicacionExistente.getAuto();
        FichaTecnica fichaExistente = autoExistente.getFichaTecnica();

        if(dto.getDescripcion() != null && !dto.getDescripcion().trim().isEmpty()){
            publicacionExistente.setDescripcion(dto.getDescripcion().trim());
        }

        if(dto.getAuto() != null){
            AutoRequestDTO autoDTO = dto.getAuto();

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

            if(autoDTO.getFichaTecnica() != null){
                FichaTecnicaRequestDTO fichaDTO = autoDTO.getFichaTecnica();

                if(fichaDTO.getMotor() != null && !fichaDTO.getMotor().trim().isEmpty()){
                    fichaExistente.setMotor(fichaDTO.getMotor().trim());
                }
                if(fichaDTO.getCombustible() != null && !fichaDTO.getCombustible().trim().isEmpty()){
                    fichaExistente.setCombustible(fichaDTO.getCombustible().trim());
                }
                if(fichaDTO.getCaja() != null && !fichaDTO.getCaja().trim().isEmpty()){
                    fichaExistente.setCaja(fichaDTO.getCaja().trim());
                }
                if(fichaDTO.getPuertas() != null && !fichaDTO.getPuertas().trim().isEmpty()){
                    fichaExistente.setPuertas(fichaDTO.getPuertas().trim());
                }
                if(fichaDTO.getPotencia() != null && !fichaDTO.getPotencia().trim().isEmpty()){
                    fichaExistente.setPotencia(fichaDTO.getPotencia().trim());
                }
            }
        }

        List<String> urlsCombinadas = new ArrayList<>();

        if (dto.getAuto() != null && dto.getAuto().getImagenesUrl() != null) {
            for (String urlVieja : dto.getAuto().getImagenesUrl()) {
                if (urlVieja != null && !urlVieja.trim().isEmpty()) {
                    // Le quitamos el dominio para mantener la ruta relativa en la BD
                    String urlLimpia = urlVieja.replace("http://localhost:8080", "");
                    urlsCombinadas.add(urlLimpia);
                }
            }
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String nuevaUrl = imageStorageService.store(file);
                    urlsCombinadas.add(nuevaUrl);
                }
            }
        }

        autoExistente.setImagenesUrl(urlsCombinadas);

        if(fichaExistente != null) {
            fichaTecnicaRepository.save(fichaExistente);
        }
        autoRepository.save(autoExistente);
        Publicacion publicacionGuardada = publicacionRepository.save(publicacionExistente);

        return PublicacionMapper.toResponseDTO(publicacionGuardada);
    }

    @Transactional
    public void deletePublicacion(Long idPublicacion, String emailVendedor){
        Publicacion publicacionExistente = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));

        Usuario vendedor = usuarioRepository.findByemail(emailVendedor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));


        Usuario vendedorPublicacion = publicacionExistente.getVendedor();

        if (vendedorPublicacion == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "La publicacion está rota (no tiene vendedor asignado en la BD). Eliminála manualmente desde la base de datos.");
        }

        if(!vendedorPublicacion.getId().equals(vendedor.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta publicacion.");
        }

        Auto auto = publicacionExistente.getAuto();
        FichaTecnica ficha = (auto != null) ? auto.getFichaTecnica() : null;

        publicacionRepository.eliminarDeTodosLosFavoritos(idPublicacion);
        publicacionRepository.eliminarReservasDePublicacion(idPublicacion);

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
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada."));

        if(publicacion.getEstado() != EstadoPublicacion.PENDIENTE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicación no está pendiente de aprobación");
        }

        publicacion.setEstado(EstadoPublicacion.ACEPTADA);
        Publicacion publicacionAprobada = publicacionRepository.save(publicacion);

        String emailVendedor = publicacion.getVendedor().getEmail();
        String nombreAuto = publicacion.getAuto().getMarca() + " " + publicacion.getAuto().getModelo();

        emailService.sendEmail(emailVendedor, "¡Tu publicación fue aprobada!", "Hola, tu publicación del " + nombreAuto + " en 'MyCar' ha sido aprobada. ¡Esperamos que puedas vender tu auto pronto!");

        return PublicacionMapper.toResponseDTO(publicacionAprobada);
    }

    @Transactional
    public PublicacionResponseDTO rechazarPublicacion(Long idPublicacion){
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada."));

        if(publicacion.getEstado() != EstadoPublicacion.PENDIENTE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicacion no esta pendiente para ser rechazada");
        }

        publicacion.setEstado(EstadoPublicacion.RECHAZADA);
        Publicacion publicacionRechazada = publicacionRepository.save(publicacion);

        String emailVendedor = publicacion.getVendedor().getEmail();
        String nombreAuto = publicacion.getAuto().getMarca() + " " + publicacion.getAuto().getModelo();

        emailService.sendEmail(emailVendedor, "Publicación rechazada", "Hola, tu publicación del " + nombreAuto + " en 'MyCar' ha sido rechazada. Por favor, revisa que los datos, imágenes y videos sean correctos antes de volver a enviarla.");

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

        List<Object[]> topMarcasResult = publicacionRepository.findTopMarcas();
        java.util.Map<String, Long> topMarcasMap = new java.util.LinkedHashMap<>();
        
        int limit = 0;
        for (Object[] row : topMarcasResult) {
            if (limit >= 5) break;
            String marca = (String) row[0];
            Long count = (Long) row[1];
            marca = marca.trim();
            marca = marca.substring(0, 1).toUpperCase() + marca.substring(1).toLowerCase();
            topMarcasMap.put(marca, topMarcasMap.getOrDefault(marca, 0L) + count);
            limit++;
        }

        return PublicacionEstadisticasDTO.builder()
                .totalPublicaciones(total)
                .pendientes(pendientes)
                .aceptadas(aceptadas)
                .rechazadas(rechazadas)
                .usuario(porUsuario)
                .concesionaria(porConcesionaria)
                .topMarcas(topMarcasMap)
                .build();
    }

    @Transactional
    public void deletePublicacionAdmin(Long idPublicacion){
        Publicacion publicacionExistente = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));

        List<Reserva> reservas = reservaRepository.findByPublicacion_Id(idPublicacion);
        if (reservas != null && !reservas.isEmpty()) {
            reservaRepository.deleteAll(reservas);
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
}