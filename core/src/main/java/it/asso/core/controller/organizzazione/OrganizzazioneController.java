package it.asso.core.controller.organizzazione;

import it.asso.core.common.Def;
import it.asso.core.common.ResultGrid;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.organizzazione.OrganizzazioneDAO;
import it.asso.core.dao.organizzazione.TipoOrganizzazioneDAO;
import it.asso.core.model.organizzazione.*;
import it.asso.core.service.OrganizzazioneService; // Nuovo Service Layer
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/jsp/private/organizzazione")
public class OrganizzazioneController { // Non estende BaseController

    // 1. INIEZIONE DEL SERVICE E DEI DAO
    private final OrganizzazioneService organizzazioneService;
    private final OrganizzazioneDAO organizzazioneDao;
    private final TipoOrganizzazioneDAO tipoOrganizzazioneDAO;
    private final DocumentoDAO documentoDao;

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String WRITE_PERMISSION = "@authz.checkWritePermission('AREA_DATI_ASSOCIAZIONE')";
    private static final String AUTH = "isAuthenticated()";

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public OrganizzazioneController(OrganizzazioneService organizzazioneService, OrganizzazioneDAO organizzazioneDao, TipoOrganizzazioneDAO tipoOrganizzazioneDAO, DocumentoDAO documentoDao) {
        this.organizzazioneService = organizzazioneService;
        this.organizzazioneDao = organizzazioneDao;
        this.tipoOrganizzazioneDAO = tipoOrganizzazioneDAO;
        this.documentoDao = documentoDao;
    }

    // ------------------------------------------------------------------------
    // ENDPOINT CRUD (Organizzazione)
    // ------------------------------------------------------------------------

    // TODO da eliminare dopo passaggio ad angular 7 (Mantenuto solo per la compatibilità con il frontend)
    @RequestMapping(value = "/getOrganizzazione.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody Organizzazione getOrganizzazione() throws AssoServiceException {
        return organizzazioneDao.getByID(Def.NUM_UNO);
    }

    @RequestMapping(value = "/getOrganizzazioneById.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody Organizzazione getOrganizzazioneById(@RequestParam String id) throws AssoServiceException{
        // Logica di popolamento spostata nel Service
        return organizzazioneService.getOrganizzazioneWithDetails(id);
    }

    @RequestMapping(value = "/getOrganizzazioni.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Organizzazione> getOrganizzazioni() throws AssoServiceException{
        return organizzazioneDao.getAll();
    }

    @RequestMapping(value = "/saveOrganizzazione.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> saveOrganizzazione(@RequestBody Organizzazione organizzazione) throws AssoServiceException{
        String result = organizzazioneDao.saveOrUpdate(organizzazione);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // ------------------------------------------------------------------------
    // TIPO ORGANIZZAZIONE
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getTipiOrganizzazione.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<TipoOrganizzazione> getTipiOrganizzazione() throws AssoServiceException{
        return tipoOrganizzazioneDAO.getTipoOrganizzazione();
    }

    // ------------------------------------------------------------------------
    // PROTOCOLLI
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getProtocolli.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getProtocolli(@RequestBody RicercaDTO ricerca) throws AssoServiceException{
        // Logica di popolamento e conteggio delegata al Service
        return organizzazioneService.getProtocolli(ricerca);
    }

    @RequestMapping(value = "/getProtocolloByID.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody Protocollo getProtocolloByID(@RequestParam String idProtocollo) throws AssoServiceException{
        // Logica di popolamento documento delegata al Service
        return organizzazioneService.getProtocolloWithDocument(idProtocollo);
    }

    // ------------------------------------------------------------------------
    // GRAFICI (LETTURA)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getEntrateUscite.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody GraficoABarre getEntrateUscite() throws AssoServiceException{
        return organizzazioneDao.getEntrateUscite();
    }

    @RequestMapping(value = "/getStalliPensione.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody GraficoABarre getStalliPensione() throws AssoServiceException{
        return organizzazioneDao.getStalliPensione();
    }

    @RequestMapping(value = "/getDocumentiAnno.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody GraficoABarre getDocumentiAnno() throws AssoServiceException{
        return organizzazioneDao.getDocumentiAnno();
    }

    @RequestMapping(value = "/getContattiAnno.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody GraficoABarre getContattiAnno() throws AssoServiceException{
        return organizzazioneDao.getContattiAnno();
    }

    @RequestMapping(value = "/getAdozioniAnno.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody GraficoABarre getAdozioniAnno() throws AssoServiceException{
        GraficoABarre gb = organizzazioneDao.getAdozioniAnno();

        // Logica di ordinamento mantenuta nel controller (potrebbe essere spostata nel Service)
        gb.getLabels().sort(Comparator.comparing(Double::parseDouble));
        gb.getData().sort(Comparator.comparing(DataSet::getLabel));
        return gb;
    }
}