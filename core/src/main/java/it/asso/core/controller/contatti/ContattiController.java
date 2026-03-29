package it.asso.core.controller.contatti;

import it.asso.core.common.Def;
import it.asso.core.common.ResultGrid;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.documenti.FileDAO;
import it.asso.core.dao.documenti.TipoDocumentoDAO;
import it.asso.core.dao.localizzazione.LocalizzazioneDAO;
import it.asso.core.model.contatto.*;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.localizzazione.Comune;
import it.asso.core.model.localizzazione.Provincia;
import it.asso.core.model.localizzazione.Regione;
import it.asso.core.security.UserAuth;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/jsp/private/contatto") // Path base per pulizia
public class ContattiController { // Non estende BaseController

    // 1. DIPENDENZE E INIEZIONE TRAMITE COSTRUTTORE
    private final ContattoDAO contattoDao;
    private final LocalizzazioneDAO amministrazioneDao;
    private final DocumentoDAO documentoDao;
    private final TipoDocumentoDAO tipoDocumentoDao;
    private final FileDAO fileDao; // Se FileDAO è ancora necessario

    public ContattiController(ContattoDAO contattoDao, LocalizzazioneDAO amministrazioneDao, DocumentoDAO documentoDao, TipoDocumentoDAO tipoDocumentoDao, FileDAO fileDao) {
        this.contattoDao = contattoDao;
        this.amministrazioneDao = amministrazioneDao;
        this.documentoDao = documentoDao;
        this.tipoDocumentoDao = tipoDocumentoDao;
        this.fileDao = fileDao;
    }

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String WRITE_PERMISSION = "@authz.checkWritePermission('AREA_DATI_CONTATTI')";

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (SAVE / DELETE)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveOrUpdate.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> save(@RequestBody Contatto contatto, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        contatto.setAccount(user.getUsername());
        String idContatto = contattoDao.saveOrUpdate(contatto);
        return ResponseEntity.ok().body("\"" + idContatto + "\"");
    }

    @RequestMapping(value = "/deleteByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> deleteByID(@RequestParam String id) throws AssoServiceException {
        String result;

        try {
            Contatto contatto = contattoDao.getByID(id, true);
            contattoDao.deleteByID(id);

            // Logica di business: eliminazione dei documenti associati
            if (contatto.getDocumenti() != null) {
                List<Documento> documenti = documentoDao.getDocumentiByIDContatto(contatto.getId_contatto());
                for (Documento doc : documenti) {
                    documentoDao.deleteByID(doc.getId_documento());
                }
            }
            result = Def.STR_OK;
        } catch (DataIntegrityViolationException e) {
            result = Def.STR_ERROR + "Contatto non eliminabile.";
        } catch (Exception e) {
            result = Def.STR_ERROR + e.getMessage();
        }

        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (GET)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getContattiBySearch.json", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication
    public @ResponseBody ResultGrid getContattiBySearch(@RequestBody RicercaDTO ricerca) throws AssoServiceException {
        ResultGrid result = new ResultGrid();
        List<Contatto> contatti = contattoDao.getBySearch(ricerca);
        result.setRecords(contatti);
        result.setTotale(contattoDao.getCountBySearch(ricerca));
        return result;
    }

    @RequestMapping(value = "/getContatti.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Contatto> getContatti(@RequestParam("strToSearch") String strToSearch, @RequestParam("id") String id) throws AssoServiceException {
        return contattoDao.getBySearch(strToSearch, id);
    }

    @RequestMapping(value = "/getPreaffidanti.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Contatto> getPreaffidanti(@RequestParam("strToSearch") String strToSearch) throws AssoServiceException {
        return contattoDao.getPreaffidantiBySearch(strToSearch);
    }

    @RequestMapping(value = "/getFornitori.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Contatto> getFornitori(@RequestParam("strToSearch") String strToSearch) throws AssoServiceException {
        List<String> tipiContatti = new ArrayList<>();
        tipiContatti.add(Def.CONTATTO_PUNTO_VENDITA);
        tipiContatti.add(Def.CONTATTO_VETERINARIO_CLINICA);
        tipiContatti.add(Def.CONTATTO_PENSIONE);
        tipiContatti.add(Def.CONTATTO_CANILE);
        tipiContatti.add(Def.CONTATTO_FORNITORE);
        tipiContatti.add(Def.CONTATTO_ASSOCIAZIONE);
        return contattoDao.getContattiByTipoAndSearch(strToSearch, tipiContatti);
    }

    @RequestMapping(value = "/getAdottantiADistanza.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Contatto> getAdottantiADistanza(@RequestParam("strToSearch") String strToSearch) throws AssoServiceException {
        return contattoDao.getAdottantiADistanza(strToSearch);
    }

    @RequestMapping(value = "/getPuntiRaccolta.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Contatto> getPuntiRaccolta(@RequestParam("strToSearch") String strToSearch) throws AssoServiceException {
        return contattoDao.getPuntiRaccoltaBySearch(strToSearch);
    }

    @RequestMapping(value = "/getVolontarie.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Contatto> getVolontarie(@RequestParam("strToSearch") String strToSearch) throws AssoServiceException {
        return contattoDao.getVolontarieBySearch(strToSearch);
    }

    @RequestMapping(value = "/getStallanti.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Contatto> getStallanti(@RequestParam("strToSearch") String strToSearch) throws AssoServiceException {
        return contattoDao.getStallanti(strToSearch);
    }

    @RequestMapping(value = "/getRaccoglitori.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Contatto> getRaccoglitori(@RequestParam("strToSearch") String strToSearch) throws AssoServiceException {
        return contattoDao.getRaccoglitoriBySearch(strToSearch);
    }

    @RequestMapping(value = "/getReferenteByID.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Contatto getReferenteByID(@RequestParam("id") String id) throws AssoServiceException {
        Contatto contatto = contattoDao.getByID(id);
        // Logica di popolamento documenti (da spostare in Service)
        if (contatto.getDocumenti() != null) {
            List<Documento> documenti = documentoDao.getDocumentiByIDContatto(contatto.getId_contatto());
            contatto.getDocumenti().addAll(documenti);
        }
        return contatto;
    }

    @RequestMapping(value = "/getTipoReferenti.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<TipoContatto> getTipoContatto() throws AssoServiceException {
        return contattoDao.getTipoContatti();
    }

    @RequestMapping(value = "/getqualifiche.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Qualifica> getQualifiche() throws AssoServiceException {
        return contattoDao.getQualifiche();
    }

    @RequestMapping(value = "/getProvince.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Provincia> getProvince(@RequestParam String id) throws AssoServiceException {
        return amministrazioneDao.getProvincie(id);
    }

    @RequestMapping(value = "/getRegioni.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Regione> getRegioni() throws AssoServiceException {
        return amministrazioneDao.getRegioni();
    }

    @RequestMapping(value = "/getRegioniByNazione.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Regione> getRegioniByNazione(@RequestParam String nazione) throws AssoServiceException {
        return amministrazioneDao.getRegioneByNazioneUsed(nazione);
    }

    @RequestMapping(value = "/getRegioniById.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Regione> getRegioniById(@RequestParam String nazione) throws AssoServiceException {
        return amministrazioneDao.getRegioneByNazione(nazione);
    }

    @RequestMapping(value = "/getComune.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Comune getComune(@RequestParam String id) throws AssoServiceException {
        return amministrazioneDao.getComuneByID(id);
    }

    @RequestMapping(value = "/getComuni.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Comune> getComuni(@RequestParam String id) throws AssoServiceException {
        return amministrazioneDao.getComuni(id);
    }

    // ------------------------------------------------------------------------
    // CHECK PIVA (VIES) - Metodo legacy
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getPiva.json", method = RequestMethod.GET)
    @ResponseBody
    public PivaVies checkPIVA(@RequestParam String piva) {
        PivaVies o = new PivaVies();
        // Il codice Axis obsoleto e commentato è stato rimosso.
        // La logica di VIES va migrata a un client HTTP moderno (WebClient/RestTemplate)
        // o delegata a un Service esterno.
        return o;
    }
}