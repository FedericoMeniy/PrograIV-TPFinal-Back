package concesionaria.example.Concesionaria.service;

import concesionaria.example.Concesionaria.dto.AutoResponseDTO;
import concesionaria.example.Concesionaria.dto.FichaTecnicaResponseDTO;
import concesionaria.example.Concesionaria.dto.PublicacionResponseDTO;
import concesionaria.example.Concesionaria.entity.Auto;
import concesionaria.example.Concesionaria.entity.FichaTecnica;
import concesionaria.example.Concesionaria.entity.Publicacion;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase de ayuda (Utility) para convertir Entidades a DTOs de respuesta.
 */
public class PublicacionMapper {

    // Convierte UNA Publicacion (Entidad) a UNA PublicacionResponseDTO
    public static PublicacionResponseDTO toResponseDTO(Publicacion publicacion) {
        PublicacionResponseDTO dto = new PublicacionResponseDTO();
        dto.setId(publicacion.getId());
        dto.setDescripcion(publicacion.getDescripcion());
        dto.setEstado(publicacion.getEstado());
        dto.setTipoPublicacion(publicacion.getTipoPublicacion());
        dto.setNombreVendedor(publicacion.getVendedor().getNombre()); // <-- Mapeo seguro
        dto.setAuto(toAutoResponseDTO(publicacion.getAuto())); // Mapeo anidado
        return dto;
    }

    // Convierte UNA LISTA de Entidades a UNA LISTA de DTOs
    public static List<PublicacionResponseDTO> toResponseDTOList(List<Publicacion> publicaciones) {
        return publicaciones.stream()
                .map(PublicacionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Métodos privados de ayuda para los objetos anidados
    private static AutoResponseDTO toAutoResponseDTO(Auto auto) {
        AutoResponseDTO dto = new AutoResponseDTO();
        dto.setId(auto.getId());
        dto.setMarca(auto.getMarca());
        dto.setModelo(auto.getModelo());
        dto.setPrecio(auto.getPrecio());
        dto.setAnio(auto.getAnio());
        dto.setKm(auto.getKm());
        dto.setColor(auto.getColor());
        dto.setFichaTecnica(toFichaTecnicaResponseDTO(auto.getFichaTecnica()));
        return dto;
    }

    private static FichaTecnicaResponseDTO toFichaTecnicaResponseDTO(FichaTecnica ficha) {
        FichaTecnicaResponseDTO dto = new FichaTecnicaResponseDTO();
        dto.setId(ficha.getId());
        dto.setMotor(ficha.getMotor());
        dto.setCombustible(ficha.getCombustible());
        dto.setCaja(ficha.getCaja());
        dto.setPuertas(ficha.getPuertas());
        dto.setPotencia(ficha.getPotencia());
        return dto;
    }
}
