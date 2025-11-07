package concesionaria.example.Concesionaria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvnConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Permite acceder a los archivos en la carpeta 'uploads'
        // a través de la URL '/images/**'
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/");
    }
}