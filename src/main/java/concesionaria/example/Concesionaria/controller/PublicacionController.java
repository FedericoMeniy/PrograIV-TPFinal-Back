package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.PublicacionRequestDTO;
import concesionaria.example.Concesionaria.dto.PublicacionResponseDTO;
import concesionaria.example.Concesionaria.entity.Publicacion;
import concesionaria.example.Concesionaria.service.PublicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publicacion")
public class PublicacionController {
    @Autowired
    private PublicacionService publicacionService;

    @GetMapping("/misPublicaciones")
    public List<PublicacionResponseDTO> getMisPublicaciones(@RequestParam Long idUsuario){
        return publicacionService.getPublicacion(idUsuario);
    }

    @PostMapping("/crearPublicacion")
    public PublicacionResponseDTO postPublicacion(@RequestBody PublicacionRequestDTO publicacionDTO, @RequestParam Long idUsuario){
        return publicacionService.postPublicacion(publicacionDTO, idUsuario);
    }

    @PutMapping("/{id}")
    public PublicacionResponseDTO putPublicacion(@PathVariable Long id, @RequestBody PublicacionRequestDTO publicacionDTO, @RequestParam Long idUsuario){
        return publicacionService.putPublicacion(id, publicacionDTO, idUsuario);
    }

    @DeleteMapping("/{id}")
    public void deletePublicacion(@PathVariable Long id, @RequestParam Long idUsuario){
        publicacionService.deletePublicacion(id, idUsuario);
    }
}
