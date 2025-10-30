package concesionaria.example.Concesionaria.entity;

import concesionaria.example.Concesionaria.enums.Rol;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Entity
public class Usuario implements UserDetails { // Implementar UserDetails
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String email;
    private String password;

    @OneToMany(mappedBy = "vendedor")
    private List<Publicacion> publicaciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Reserva> reservas = new ArrayList<>();
    private Rol rol;

    // --- Implementación de UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Se mapea el rol a una autoridad para Spring Security
        return List.of(new SimpleGrantedAuthority(rol.name()));
    }

    @Override
    public String getUsername() {
        // Usamos el email como el identificador único (username)
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}