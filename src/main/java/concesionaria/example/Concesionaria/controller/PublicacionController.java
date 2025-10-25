package concesionaria.example.Concesionaria.controller;


import concesionaria.example.Concesionaria.service.PublicacionService;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publicacion")
public class PublicacionController {
    private PublicacionService publicacionService;



}
