package concesionaria.example.Concesionaria.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import concesionaria.example.Concesionaria.dto.PublicacionRequestDTO;
import concesionaria.example.Concesionaria.dto.PublicacionResponseDTO;
import concesionaria.example.Concesionaria.service.PublicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/publicacion")
public class PublicacionController {
    @Autowired
    private PublicacionService publicacionService;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/misPublicaciones")
    public List<PublicacionResponseDTO> getMisPublicaciones(Authentication authentication){
        String emailVendedor = authentication.getName();
        return publicacionService.getPublicacion(emailVendedor);
    }

    @GetMapping("/tienda")
    public List<PublicacionResponseDTO> getCatalogoTienda(){
        return publicacionService.getCatalogoTienda();
    }

    @GetMapping("/usados")
    public List<PublicacionResponseDTO> getCatalogoUsados(){
        return publicacionService.getCatalogoUsados();
    }

    @GetMapping("/{id}")
    public PublicacionResponseDTO getPublicacionById(@PathVariable Long id){
        return publicacionService.getPublicacionById(id);
    }

    @PostMapping("/crearPublicacion")
    public PublicacionResponseDTO postPublicacion(
            @RequestParam("publicacion") String publicacionDtoString, // El DTO como String
            @RequestParam(value = "files", required = false) List<MultipartFile> files, // Los archivos
            Authentication authentication) {

        String emailVendedor = authentication.getName();

        PublicacionRequestDTO publicacionDTO;
        try {
            publicacionDTO = objectMapper.readValue(publicacionDtoString, PublicacionRequestDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear los datos de la publicación", e);
        }

        return publicacionService.postPublicacion(publicacionDTO, files, emailVendedor);
    }

    @PutMapping("/{id}")
    public PublicacionResponseDTO putPublicacion(@PathVariable Long id, @RequestBody PublicacionRequestDTO publicacionDTO, Authentication authentication){
        String emailVendedor = authentication.getName();
        return publicacionService.putPublicacion(id, publicacionDTO, emailVendedor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePublicacion(@PathVariable Long id, Authentication authentication){
        String emailVendedor = authentication.getName();
        publicacionService.deletePublicacion(id, emailVendedor);

        Map<String, String> response = Map.of("mensaje", "Publicación eliminada correctamente");
        return ResponseEntity.ok(response);
    }

    // Lo que dice corregido lo cambie yo para poder hacer las funcionalidades del admin en el front
    //Fede 12/11 21:04
    // [CORREGIDO] Ruta /admin/pendientes para que coincida con SecurityConfig
    @GetMapping("/admin/pendientes")
    public List<PublicacionResponseDTO> getPublicacionesPendientes(){
        return publicacionService.getPublicacionesPendientes();
    }

    // [CORREGIDO] Ruta /admin/aprobar/{id} para que coincida con SecurityConfig
    @PatchMapping("/admin/aprobar/{id}")
    public PublicacionResponseDTO aprobarPublicacion(@PathVariable Long id) {
        return publicacionService.aprobarPublicacion(id);
    }

    // [CORREGIDO] Ruta /admin/rechazar/{id} para que coincida con SecurityConfig
    @DeleteMapping("/admin/rechazar/{id}") // Cambiado de PatchMapping a DeleteMapping para coincidir con la acción de "eliminar/rechazar"
    public ResponseEntity<?> rechazarPublicacion(@PathVariable Long id){
        publicacionService.rechazarPublicacion(id);
        Map<String, String> response = Map.of("mensaje", "Publicación rechazada y eliminada correctamente");
        return ResponseEntity.ok(response);
    }

    // Nuevo endpoint para que el usuario marque como vendida su publicación (que resulta en eliminación)
    @PatchMapping("/vendida/{id}")
    public ResponseEntity<?> marcarComoVendidaYEliminar(@PathVariable Long id, Authentication authentication){
        String emailVendedor = authentication.getName();
        publicacionService.marcarComoVendidaYEliminar(id, emailVendedor);

        Map<String, String> response = Map.of("mensaje", "Publicación marcada como vendida y eliminada correctamente.");
        return ResponseEntity.ok(response);
    }
}