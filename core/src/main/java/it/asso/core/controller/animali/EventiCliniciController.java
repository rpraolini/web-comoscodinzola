package it.asso.core.controller.animali;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.salute.EventoClinicoDAO;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.model.animali.salute.EventoClinico;
import it.asso.core.model.animali.salute.TipoEvento;
import it.asso.core.service.EventiCliniciService; // Nuovo Service
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.sql.SQLIntegrityConstraintViolationException;

@Controller
@RequestMapping("/jsp/private/animale") // Path base
public class EventiCliniciController { // Non estende BaseController

    // 1. INIEZIONE DEL SERVICE (per logica complessa/popolamento)
    private final EventiCliniciService eventiCliniciService;
    private final EventoClinicoDAO eventoClinicoDao; // Mantenuto per save/delete

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public EventiCliniciController(EventiCliniciService eventiCliniciService, EventoClinicoDAO eventoClinicoDao) {
        this.eventiCliniciService = eventiCliniciService;
        this.eventoClinicoDao = eventoClinicoDao;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (DELEGATI AL SERVICE)
    // ------------------------------------------------------------------------

    // Logica di popolamento spostata nel Service
    @RequestMapping(value = "/getEventiCliniciByIdAnimale.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody List<EventoClinico> getEventiCliniciByAnimale(@RequestParam String id) throws AssoServiceException {
        // La logica di caricamento dei documenti incrociati è nel Service
        return eventiCliniciService.getEventiCliniciByIdAnimale(id);
    }

    // Metodo ripetuto in AnagraficheController - mantenuto
    @RequestMapping(value = "/getTipoEventiClinici.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<TipoEvento> getTipoEventiClinici(@RequestParam String id) throws AssoServiceException {
        return eventoClinicoDao.getTipiEventi(id);
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (UTILIZZANO @PreAuthorize E ResponseEntity)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveOrUpdateEventoClinico.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> saveOrUpdateEventoClinico(@RequestBody EventoClinico evento) throws AssoServiceException, ParseException {
        // La logica di business e il salvataggio sono nel Service (o nel DAO)
        String result = eventoClinicoDao.saveOrUpdate(evento);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }


    @RequestMapping(value = "/delEventoClinicoById.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> delEventoClinicoById(@RequestParam String id)
            throws AssoServiceException, SQLIntegrityConstraintViolationException, IllegalStateException, IOException {

        // La logica complessa di eliminazione (documenti, foto, evento) deve essere nel Service.
        // Se non implementi il service, la logica deve essere qui:
       /*
       List<Documento> documenti = documentoDao.getDocumentiByIDEvento(id);
       for (Documento doc : documenti) {
          documentoDao.deleteDocumentoEventoClinico(doc.getId_documento(), id);
          documentoDao.deleteByID(doc.getId_documento());
       }
       String result = eventoClinicoDao.delete(id);
       */

        // DELEGAZIONE AL SERVICE (Metodo da implementare)
        String result = eventiCliniciService.deleteEventoClinicoAndDocuments(id);

        return ResponseEntity.ok().body("\"" + result + "\"");
    }

}