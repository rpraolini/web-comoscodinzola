package it.asso.core.controller;

import it.asso.core.dto.login.LoginRequest;
import it.asso.core.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 1. Spring Security fa tutto il lavoro sporco qui!
            // Cerca l'utente, confronta la password criptata e, se va male, lancia un'eccezione.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 2. Se arriviamo a questa riga, le credenziali sono perfette! Generiamo il Token
            String token = jwtUtils.generateToken(authentication.getName());

            // Estraiamo il ruolo per mandarlo ad Angular
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", authentication.getName(),
                    "role", role
            ));

        } catch (AuthenticationException e) {
            // 3. Se la password è sbagliata o l'utente non esiste, atterriamo qui
            System.out.println("---- MOTIVO FALLIMENTO LOGIN ----");
            System.out.println("Errore: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Username o password errati"));
        }
    }
}