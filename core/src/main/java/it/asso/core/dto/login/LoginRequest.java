package it.asso.core.dto.login;

public class LoginRequest {
    private String username;
    private String password;

    // Getter e Setter obbligatori
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}