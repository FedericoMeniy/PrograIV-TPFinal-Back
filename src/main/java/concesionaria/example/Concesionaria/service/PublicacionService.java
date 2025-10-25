package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicacionService {
    private  PublicacionRepository publicacionRepository;
}
