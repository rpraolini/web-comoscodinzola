package it.asso.core.controller.utente;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.controller.BaseController;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.utente.UtenteDAO;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.utente.AreaDati;
import it.asso.core.model.utente.Ruolo;
import it.asso.core.model.utente.Utente;
import it.asso.core.security.UserAuth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jsp/private") // Imposta un path base per pulire le URL
public class UserController extends BaseController {

    // Iniezione tramite Costruttore (Best Practice)
    private final UtenteDAO utenteDao;
    private final ContattoDAO contattoDao;

    public UserController(UtenteDAO utenteDao, ContattoDAO contattoDao) {
        this.utenteDao = utenteDao;
        this.contattoDao = contattoDao;
    }

    // 1. SOLO AUTENTICAZIONE (Utilizza @AuthenticationPrincipal)
    @RequestMapping(value = "/getUtente.json", method = RequestMethod.GET)
    public @ResponseBody Utente getUtente(@AuthenticationPrincipal UserAuth user) {
        // Nessun check di autenticazione necessario, Spring lo fa prima
        return user.getUtente();
    }

    // 2. CONTROLLO RUOLO + PERMESSI (Trasferito a @PreAuthorize)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/getAllUsers.json", method = RequestMethod.GET)
    public @ResponseBody List<Utente> getAllUsers() {
        return utenteDao.getAllUsers();
    }

    // 3. CONTROLLO RUOLO + PERMESSI
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/getUserByID.json", method = RequestMethod.POST)
    public @ResponseBody Utente getUserByID(@RequestParam String id) {
        return utenteDao.getUtenteByID(id);
    }

    // 4. CONTROLLO RUOLO + PERMESSI (Applicato come gli altri)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/getUsersBySearch.json", method = RequestMethod.POST)
    public @ResponseBody List<Utente> getUsersBySearch(@RequestParam String search) {
        return utenteDao.getUsersBySearch(search);
    }

    // 5. OPERAZIONE COMPLESSA: SAVE (Applica PreAuthorize)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/utente/saveUtente.json", method = RequestMethod.POST)
    public @ResponseBody Utente saveUtente(@RequestParam String idContatto) throws AssoServiceException {
        // Logica interna del metodo mantenuta
        Utente result = utenteDao.getUtenteByIDContatto(idContatto);
        if (result == null) {
            Contatto c = contattoDao.getByID(idContatto, true);
            String id = utenteDao.saveUtente(c);
            utenteDao.insertRuolo(Def.STR_ROLE_GUEST, id);
            result = utenteDao.getUtenteByID(id);
            utenteDao.insertPermessi(result);
        } else {
            throw new AssoServiceException(Def.STR_ERROR_600); // Lancia un'eccezione, gestita da Spring
        }
        return result;
    }

    // 6. DELETE (Usa ResponseEntity per risposte HTTP standard)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/utente/deleteUser.json", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> deleteUser(@RequestParam String id) {
        if (Def.NUM_UNO.equals(id)) {
            // 400 Bad Request
            return ResponseEntity.badRequest().body("\"KO: Non puoi eliminare l'utente principale\"");
        }

        String result = utenteDao.deleteUser(id);

        if ("OK".equals(result)) {
            // 200 OK
            return ResponseEntity.ok().body("\"OK\"");
        } else {
            // 500 Internal Server Error o 404 Not Found, a seconda della logica
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("\"ERRORE: Eliminazione fallita\"");
        }
    }

    // 7. ENABLE (Conversione a ResponseEntity)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/utente/enableUser.json", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> enableUser(@RequestParam String id) {
        if (Def.NUM_UNO.equals(id)) {
            return ResponseEntity.badRequest().body("\"KO\"");
        }
        String result = utenteDao.enableUser(id);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // 8. DISABLE (Conversione a ResponseEntity)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/utente/disableUser.json", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> disableUser(@RequestParam String id) {
        if (Def.NUM_UNO.equals(id)) {
            return ResponseEntity.badRequest().body("\"KO\"");
        }
        String result = utenteDao.disableUser(id);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // 9. SAVE RUOLO (Applica PreAuthorize)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/saveRuolo.json", method = RequestMethod.POST)
    public @ResponseBody Utente saveRuolo(@RequestBody Utente utente) throws AssoServiceException {
        if (Def.NUM_UNO.equals(utente.getId_utente())) {
            return utente;
        }
        try {
            return utenteDao.saveRuolo(utente);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    // 10. SAVE PERMESSI (Applica PreAuthorize)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/savePermessi.json", method = RequestMethod.POST)
    public @ResponseBody Utente savePermessi(@RequestBody Utente utente) throws AssoServiceException {
        try {
            return utenteDao.savePermessi(utente);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    // 11. GET RUOLI (Applica PreAuthorize)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/getRuoli.json", method = RequestMethod.POST)
    public @ResponseBody List<Ruolo> getRuoli() {
        return utenteDao.getRuoli();
    }

    // 12. SAVE AREA (Conversione a ResponseEntity)
    @PreAuthorize("hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_UTENTE')")
    @RequestMapping(value = "/saveArea.json", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> saveArea(@RequestParam String area) throws AssoServiceException {
        try {
            AreaDati a = new AreaDati();
            a.setArea(area.toUpperCase());
            String result = utenteDao.saveArea(a);
            return ResponseEntity.ok().body("\"" + result + "\"");
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

}