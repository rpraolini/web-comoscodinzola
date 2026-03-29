package it.asso.core.controller;

import it.asso.core.security.UserAuth;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// Rimuovi @Controller se non hai bisogno che sia un bean
public abstract class BaseController {

    /**
     * Metodo minimale per ottenere l'utente autenticato in modo sicuro.
     * Non gestisce l'eccezione, delega l'errore a Spring Security se l'utente non è loggato.
     */
    public UserAuth getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se l'utente è autenticato e non anonimo
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserAuth) {
            return (UserAuth) auth.getPrincipal();
        }
        // Se non autenticato o anonimo, restituisce null (o lancia una SecurityException)
        return null;
    }
}