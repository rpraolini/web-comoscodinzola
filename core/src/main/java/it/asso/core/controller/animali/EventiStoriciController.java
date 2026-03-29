package it.asso.core.controller.animali;

import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.storia.EventoStoricoDAO;
import it.asso.core.model.animali.storia.EventoStorico;
import it.asso.core.model.animali.storia.TipoEventoStorico;
import it.asso.core.security.UserAuth;
import it.asso.core.service.EventiStoriciService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jsp/private/animale") // Path base
public class EventiStoriciController { // Non estende BaseController

    // 1. INIEZIONE DEL SERVICE (per logica complessa/popolamento/transazioni)
    private final EventiStoriciService eventiStoriciService;

    // Manteniamo solo i DAO necessari per le operazioni semplici non delegate
    private final EventoStoricoDAO eventoStoricoDao;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public EventiStoriciController(EventiStoriciService eventiStoriciService, EventoStoricoDAO eventoStoricoDao) {
        this.eventiStoriciService = eventiStoriciService;
        this.eventoStoricoDao = eventoStoricoDao;
    }

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String WRITE_PERMISSION = "@authz.checkWritePermission('AREA_DATI_ANIMALE')";

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (DELEGATI AL SERVICE)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getEventiStoriciByIdAnimale.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody List<EventoStorico> getEventiStoriciByIdAnimale(@RequestParam String id) throws AssoServiceException {
        // La logica di caricamento e popolamento dei contatti è nel Service
        return eventiStoriciService.getEventiStoriciByIdAnimale(id);
    }

    @RequestMapping(value = "/getTipiEventiStorici.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody List<TipoEventoStorico> getTipiEventiStorici() throws AssoServiceException {
        return eventoStoricoDao.getTipiEventiStorici();
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (UTILIZZANO @PreAuthorize E ResponseEntity)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveOrUpdateEventoStorico.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission()
    public @ResponseBody ResponseEntity<String> saveOrUpdateEventoStorico(@RequestBody EventoStorico evento, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {

        // La logica complessa di salvataggio e aggiornamento del proprietario è nel Service
        String result = eventiStoriciService.saveOrUpdateEventoStorico(evento, user);

        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/eliminaEventoStorico.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission()
    public @ResponseBody ResponseEntity<String> eliminaEventoStorico(@RequestParam String id) throws AssoServiceException {
        String result = eventoStoricoDao.delete(id);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }
}