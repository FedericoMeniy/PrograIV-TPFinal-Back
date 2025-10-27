package concesionaria.example.Concesionaria.dto;

// No necesitas @Valid aquí, solo Getters y Setters
// (Puedes usar @Data de Lombok si lo tienes)
public class LoginUsuarioDTO {
    private String email;
    private String password;

    // Getters y Setters
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}