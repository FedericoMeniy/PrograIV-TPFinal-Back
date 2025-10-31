package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.PublicacionRequestDTO;
import concesionaria.example.Concesionaria.dto.PublicacionResponseDTO;
import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.service.PublicacionService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/publicacion")
public class PublicacionController {
    @Autowired
    private PublicacionService publicacionService;

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
    public PublicacionResponseDTO postPublicacion(@RequestBody PublicacionRequestDTO publicacionDTO, Authentication authentication){
        String emailVendedor = authentication.getName();
        return publicacionService.postPublicacion(publicacionDTO, emailVendedor);
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
}
