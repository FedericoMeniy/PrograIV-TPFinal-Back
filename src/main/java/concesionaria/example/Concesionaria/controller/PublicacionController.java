package concesionaria.example.Concesionaria.controller;

import concesionaria.example.Concesionaria.service.PublicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publicacion")
@RequiredArgsConstructor
public class PublicacionController {
    private final PublicacionService publicacionService;
}
