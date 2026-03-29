package it.asso.core.security;

import it.asso.core.model.utente.Utente;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class UserAuth implements UserDetails {
    private final Utente utente; // Il tuo modello del DB

    public UserAuth(Utente utente) {
        this.utente = utente;
    }

    // Metodo magico per recuperare l'ID o tutto l'oggetto Utente nel controller
    public Utente getUtente() {
        return utente;
    }

    @Override
    public String getUsername() { return utente.getAccount(); }

    @Override
    public String getPassword() { return utente.getPwd(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mappatura ruoli come facevi nel service
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isEnabled() { return "1".equals(utente.getAbilitato()); }

    // Implementa gli altri metodi (accountNonExpired, ecc.) ritornando true
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}