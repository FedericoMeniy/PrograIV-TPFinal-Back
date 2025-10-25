package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.dto.RegistroUsuarioDto;
import concesionaria.example.Concesionaria.entity.Usuario;
import concesionaria.example.Concesionaria.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired; // 1. IMPORTAR
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private UsuarioService usuarioService;


    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroUsuarioDto registroUsuarioDto){

        try{
            Usuario usuarioRegistrado = usuarioService.registrarUsuario(registroUsuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRegistrado);

        }catch (RuntimeException e){

            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}