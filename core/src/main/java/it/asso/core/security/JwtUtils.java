package it.asso.core.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // CHIAVE SEGRETA MOLTO IMPORTANTE! In produzione usane una da variabili d'ambiente.
    // Deve essere lunga almeno 256 bit (32 caratteri)
    private static final String SECRET = "ChiaveSegretaSuperSicuraPerIlTuoGestionale123456!";
    private static final long EXPIRATION_TIME = 86400000; // 24 ore in millisecondi

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Genera il Token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Estrae l'username dal Token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Valida il Token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false; // Token scaduto o manomesso
        }
    }
}