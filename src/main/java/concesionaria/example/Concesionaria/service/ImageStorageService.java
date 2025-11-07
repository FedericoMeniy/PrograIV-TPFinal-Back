package concesionaria.example.Concesionaria.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    // Directorio donde se guardarán las imágenes
    private final Path rootLocation = Paths.get("uploads");

    public ImageStorageService() {
        try {
            // Crea el directorio si no existe
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento de imágenes", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Error: archivo vacío");
            }

            // Generar un nombre de archivo único
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Guardar el archivo en el directorio 'uploads'
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));

            // Devolver la URL pública (la usaremos en el paso 1.4)
            return "/images/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }
}