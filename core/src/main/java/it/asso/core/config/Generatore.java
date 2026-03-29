package it.asso.core.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Generatore {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // Scrivi qui ESATTAMENTE la password che vuoi usare
        String miaPassword = "admin123";
        System.out.println("Copia questo hash nel DB:");
        System.out.println(encoder.encode(miaPassword));
    }
}
