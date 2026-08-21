package concesionaria.example.Concesionaria.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageStorageService {

    private final Cloudinary cloudinary;

    public ImageStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret,
                "secure",     true
        ));
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Error: archivo vacío");
            }

            String originalFilename = file.getOriginalFilename();
            String resourceType = "";

            if (originalFilename != null) {
                String lower = originalFilename.toLowerCase();

                if (lower.endsWith(".mp4") || lower.endsWith(".mov") ||
                    lower.endsWith(".avi") || lower.endsWith(".webm")) {
                    resourceType = "video";
                }
                else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                        lower.endsWith(".png") || lower.endsWith(".webp")) {
                    resourceType = "image";
                }
                else {
                    throw new RuntimeException("Formato no soportado. Subí una foto (.jpg, .jpeg, .png y .webp) o un video (.mp4, .mov, .avi y .webm).");
                }
            }else {
                throw new RuntimeException("El archivo no tiene un nombre válido.");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", resourceType)
            );

            // Devuelve la URL publica de Cloudinary (accesible desde cualquier PC)
            return (String) result.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Error al subir el archivo a Cloudinary", e);
        }
    }
}