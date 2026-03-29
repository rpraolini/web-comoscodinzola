package it.asso.core.controller.notifiche;

import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.notifiche.NotificheDAO;
import it.asso.core.model.notifiche.Notifica;
import it.asso.core.service.NotificheService; // Nuovo Service Layer
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jsp/private/organizzazione") // Path base
public class NotificheController { // Non estende BaseController

    // 1. INIEZIONE DEL SERVICE (gestisce DAO e FCM)
    private final NotificheService notificheService;

    // COSTRUTTORE CON INIEZIONE DEL SERVICE
    public NotificheController(NotificheService notificheService) {
        this.notificheService = notificheService;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (GET)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getNotifiche.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody List<Notifica> getNotifiche(@RequestParam(required = false) String search) throws AssoServiceException {
        return notificheService.getAll(search);
    }

    // Assumendo che EventiNotifica sia una classe Model esistente
    @RequestMapping(value = "/getEventiNotifiche.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<?> getEventiNotifiche() throws AssoServiceException {
        return notificheService.getEventi();
    }

    @RequestMapping(value = "/getNotificaById.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Notifica getNotificaById(@RequestParam String id) throws AssoServiceException {
        return notificheService.getByID(id);
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (POST)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveOrUpdateNotifiche.json", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()") // Richiede solo autenticazione
    public @ResponseBody ResponseEntity<String> saveOrUpdateNotifiche(@RequestBody Notifica notifica) throws Exception {
        String result = notificheService.saveOrUpdate(notifica);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/eliminaNotifica.json", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()") // Richiede solo autenticazione
    public @ResponseBody ResponseEntity<String> eliminaNotifica(@RequestParam String id) throws AssoServiceException {
        String result = notificheService.deleteByID(id);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }


    /*---------------------------------------------------------------------------------------------------------------*/

    @RequestMapping(value = "/pushFCMNotification.json", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()") // Richiede solo autenticazione per l'invio
    public @ResponseBody ResponseEntity<String> pushFCMNotification(@RequestBody Notifica notifica) throws Exception {
        String result = notificheService.sendPushNotification(notifica);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }
}