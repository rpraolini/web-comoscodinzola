package it.asso.core.controller.organizzazione;

import it.asso.core.common.ResultGrid;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.organizzazione.contabilita.FattureDAO;
import it.asso.core.dao.organizzazione.contabilita.RendicontoDAO;
import it.asso.core.model.organizzazione.Organizzazione;
import it.asso.core.model.organizzazione.RicercaDTO;
import it.asso.core.model.organizzazione.contabilita.Fattura;
import it.asso.core.model.organizzazione.contabilita.VoceFattura;
import it.asso.core.security.UserAuth;
import it.asso.core.service.FattureService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;


@Controller
@RequestMapping("/jsp/private/organizzazione/fatture")
public class FattureController { // Non estende BaseController

    private final FattureDAO fattureDao;
    private final RendicontoDAO rendicontoDAO;
    private final DocumentoDAO documentoDao;
    private final FattureService fattureService; // Il Service Layer

    // COSTRUTTORE CON INIEZIONE
    public FattureController(FattureDAO fattureDao, RendicontoDAO rendicontoDAO, DocumentoDAO documentoDao, FattureService fattureService) {
        this.fattureDao = fattureDao;
        this.rendicontoDAO = rendicontoDAO;
        this.documentoDao = documentoDao;
        this.fattureService = fattureService;
    }

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String WRITE_PERMISSION = "@authz.checkWritePermission('AREA_DATI_ASSOCIAZIONE')";
    private static final String AUTH = "isAuthenticated()";


    /*************************** FATTURA (LETTURA) ******************************************/

    @RequestMapping(value = "/getFattureNonAssociate.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Fattura> getFattureNonAssociate() throws AssoServiceException {
        return fattureDao.getFattureNonAssociate();
    }

    @RequestMapping(value = "/getRicevuteAccontoNonAssociate.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Fattura> getRicevuteAccontoNonAssociate() throws AssoServiceException{
        return fattureDao.getRicevuteAccontoNonAssociate();
    }

    @RequestMapping(value = "/getFatture.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getFatture(HttpServletRequest request, @RequestBody RicercaDTO ricerca) throws AssoServiceException{
        // NOTA: L'accesso all'oggetto Organizzazione dalla sessione DEVE essere rimosso
        // in un'architettura moderna (o incapsulato in un Service per il multitenancy).

        Organizzazione o = (Organizzazione) request.getSession().getAttribute("tenant"); // Legacy access
        if (o == null) {
            // Placeholder: lancia un'eccezione se il tenant non è risolto.
            throw new AssoServiceException("Tenant non risolto dalla sessione.");
        }

        ResultGrid result = new ResultGrid();
        result.setRecords(fattureDao.getAll(ricerca, o.getId_organizzazione()));
        result.setTotale(fattureDao.getCountBySearch(ricerca, o.getId_organizzazione()));
        result.setTotali(fattureDao.getTotali(ricerca,  o.getId_organizzazione()));
        return result;
    }

    @RequestMapping(value = "/getFatturaById.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody Fattura getFatturaById(@RequestParam String id) throws AssoServiceException{
        Fattura m = fattureDao.getById(id);
        // Logica di popolamento documenti spostata nel Service
        return fattureService.popolaDettagliFattura(m);
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI SCRITTURA (DELEGATI AL SERVICE)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveOrUpdateFattura.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateFattura(HttpServletRequest request, @RequestBody Fattura f, @AuthenticationPrincipal UserAuth u) throws AssoServiceException{

        Organizzazione o = (Organizzazione) request.getSession().getAttribute("tenant"); // Legacy access
        if (o == null) {
            throw new AssoServiceException("Tenant non risolto.");
        }

        // Logica complessa di salvataggio, calcolo ritenuta e aggiornamento dettagli è nel Service
        String id = fattureService.saveOrUpdateFattura(f, u, o.getId_organizzazione());

        return ResponseEntity.ok().body("\"" + id + "\"");
    }

    @RequestMapping(value = "/eliminaFattura.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> eliminaFattura(@RequestBody Fattura f) throws AssoServiceException, SQLIntegrityConstraintViolationException, IllegalStateException, IOException, IOException {
        // Logica complessa di eliminazione incrociata (documenti, dettagli, ritenute collegate) è nel Service
        String id = fattureService.deleteFatturaCompletely(f);
        return ResponseEntity.ok().body("\"" + id + "\"");
    }

    @RequestMapping(value = "/getCodiceFattura.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResponseEntity<String> getCodiceMovimento(@RequestParam String anno) throws AssoServiceException{
        String codice = fattureDao.getCodiceFattura(anno);
        return ResponseEntity.ok().body("\"" + codice + "\"");
    }

    /*************************** VOCI DI FATTURA ******************************************/

    @RequestMapping(value = "/getVociFattureBySearch.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<VoceFattura> getVociFattureBySearch(@RequestParam String search) throws AssoServiceException{
        return fattureDao.getVociFattureBySearch(search);
    }

    @RequestMapping(value = "/getVociFatture.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<VoceFattura> getVociFatture() throws AssoServiceException{
        List<VoceFattura> vf = fattureDao.getAllVoceFattura();
        // Logica di popolamento rendiconto mantenuta, ma sarebbe da spostare in Service
        for(VoceFattura v : vf) {
            v.setRendiconto(rendicontoDAO.getRendicontoBySottoVoce(v.getId_cr_sotto_voce()));
        }
        return vf;
    }

    @RequestMapping(value = "/getVoceFatturaById.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody VoceFattura getVoceFatturaById(@RequestParam String id) throws AssoServiceException{
        VoceFattura m = fattureDao.getVoceFatturaById(id);
        m.setRendiconto(rendicontoDAO.getRendicontoBySottoVoce(m.getId_cr_sotto_voce()));
        return m;
    }

    @RequestMapping(value = "/saveOrUpdateVoceFattura.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateVociFattura(@RequestBody VoceFattura vf) throws AssoServiceException{
        String result = fattureDao.saveOrUpdateVoceFattura(vf);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/eliminaVoceFattura.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> eliminaVoceFattura(@RequestBody VoceFattura vf) throws AssoServiceException{
        String result = fattureDao.deleteVoceFatturaByID(vf.getId_vf());
        return ResponseEntity.ok().body("\"" + result + "\"");
    }
}