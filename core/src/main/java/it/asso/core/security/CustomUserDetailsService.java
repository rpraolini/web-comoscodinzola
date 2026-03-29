package it.asso.core.security;

import it.asso.core.dao.utente.UtenteDAO;
import it.asso.core.model.utente.Utente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Inietta qui la tua classe che fa le query al DB (es. UtenteDao o UtenteService)
    @Autowired
    private UtenteDAO utenteDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Cerchiamo l'utente nel nostro DB (adatta il nome del metodo in base al tuo DAO)
        Utente utente = utenteDao.findByUsername(username);
        /*System.out.println("---- DEBUG LOGIN ----");
        System.out.println("1. Cerca account: " + username);
        System.out.println("2. Trovato nel DB: " + (utente != null ? "SI" : "NO"));*/
        // 2. Se il DAO restituisce null, lanciamo l'errore
        if (utente == null) {
            throw new UsernameNotFoundException("Utente non trovato: " + username);
        }
        /*System.out.println("3. Hash Password (lunghezza " + (utente.getPwd() != null ? utente.getPwd().length() : 0) + "): " + utente.getPwd());
        System.out.println("4. Valore Abilitato: '" + utente.getAbilitato() + "'");*/
        // 3. Estraiamo il ruolo
        // Poiché hai una List<Ruolo>, qui per semplicità prendiamo il primo ruolo della lista
        // (Assicurati che la classe Ruolo abbia un metodo tipo getNome(), cambialo se si chiama diversamente)
        String nomeRuolo = "USER"; // Ruolo di default
        if (utente.getRuoli() != null && !utente.getRuoli().isEmpty()) {
            String ruoloDb = utente.getRuoli().get(0).getRuolo();
            if ("Amministratore".equalsIgnoreCase(ruoloDb)) {
                nomeRuolo = "ADMIN";
            } else if ("Ospite".equalsIgnoreCase(ruoloDb)) {
                nomeRuolo = "USER";
            }else {
                nomeRuolo = "NOT SET";
            }
        }

        // 4. Lo trasformiamo nell'oggetto UserDetails di Spring
        return new UserAuth(utente);
    }
}