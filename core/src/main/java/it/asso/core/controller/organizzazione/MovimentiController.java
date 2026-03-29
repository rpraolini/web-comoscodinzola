package it.asso.core.controller.organizzazione;

import it.asso.core.common.Def;
import it.asso.core.common.ResultGrid;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.controller.report.SimpleReportExporter;
import it.asso.core.controller.report.SimpleReportFiller;
import it.asso.core.dao.organizzazione.contabilita.MovimentiDAO;
import it.asso.core.dao.organizzazione.contabilita.RendicontoDAO;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.organizzazione.RicercaDTO;
import it.asso.core.model.organizzazione.contabilita.*;
import it.asso.core.security.UserAuth;
import it.asso.core.service.MovimentiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/jsp/private/organizzazione/movimenti")
public class MovimentiController { // Non estende BaseController

    // 1. DIPENDENZE
    private final MovimentiService movimentiService;
    private final MovimentiDAO movimentiDao;
    private final RendicontoDAO rendicontoDAO;
    private final SimpleReportFiller simpleReportFiller;

    // Configurazione (mantenute per i report)
    private final String pathReport;
    private final String pathPrivateAssets;

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String WRITE_PERMISSION = "@authz.checkWritePermission('AREA_DATI_ASSOCIAZIONE')";
    private static final String AUTH = "isAuthenticated()";


    // COSTRUTTORE CON INIEZIONE
    public MovimentiController(MovimentiService movimentiService, MovimentiDAO movimentiDao, RendicontoDAO rendicontoDAO, SimpleReportFiller simpleReportFiller,
                               @Value("${path_report}") String pathReport, @Value("${path_private_assets}") String pathPrivateAssets) {
        this.movimentiService = movimentiService;
        this.movimentiDao = movimentiDao;
        this.rendicontoDAO = rendicontoDAO;
        this.simpleReportFiller = simpleReportFiller;
        this.pathReport = pathReport;
        this.pathPrivateAssets = pathPrivateAssets;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (DELEGATI AL SERVICE PER LA LOGICA DI POPOLAMENTO)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getMovimentiListByAnno.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getMovimentiListByAnno(@RequestBody RicercaDTO ricerca, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        // La logica di popolamento complesso e di calcolo totale è nel Service
        return movimentiService.getMovimentiListByAnno(ricerca, user);
    }

    @RequestMapping(value = "/getMovimentoById.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody Movimento getMovimentoById(@RequestParam String id) throws AssoServiceException{
        // La logica di popolamento girofondi/documenti è nel Service
        return movimentiService.getMovimentoById(id);
    }

    @RequestMapping(value = "/getCodiceMovimento.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResponseEntity<String> getCodiceMovimento(@RequestParam String anno) throws AssoServiceException{
        String codice = movimentiDao.getCodiceMovimento(anno);
        return ResponseEntity.ok().body("\"" + codice + "\"");
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA E LOGICA COMPLESSA (DELEGATI AL SERVICE)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveMovimento.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveMovimento(@RequestBody Movimento mv, @AuthenticationPrincipal UserAuth user) throws AssoServiceException{
        // Logica di girofondi e fatture complesse è delegata al Service
        String idMovimento = movimentiService.saveMovimento(mv, user);
        return ResponseEntity.ok().body("\"" + idMovimento + "\"");
    }


    @RequestMapping(value = "/deleteMovimento.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteMovimento(@RequestBody Movimento mv) throws AssoServiceException, SQLIntegrityConstraintViolationException, IOException {
        // Logica di eliminazione transazionale e documenti è delegata al Service
        String result = movimentiService.deleteMovimento(mv);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA SEMPLICE E CONFIGURAZIONE
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getTipoMovimento.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<TipoMovimento> getTipoMovimento() throws AssoServiceException{
        return movimentiDao.getTipoMovimento();
    }

    @RequestMapping(value = "/getDestinatari.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Contatto> getDestinatari(@RequestParam String search) throws AssoServiceException{
        // La logica di estrazione dei tipi di contatto è nel Service
        return movimentiService.getDestinatari(search);
    }

    // Causali
    @RequestMapping(value = "/getCausaliByTipoMovimenti.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Causale> getCausali(@RequestParam String id) throws AssoServiceException{
        return movimentiDao.getCausaleByID(id);
    }

    // Voci di Rendiconto
    @RequestMapping(value = "/getRndSezioniAll.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<RndSezioni> getRndSezioniAll() throws AssoServiceException{
        return rendicontoDAO.getRndSezioniAll();
    }

    @RequestMapping(value = "/getRndSottoSezioniAll.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<RndSottoSezioni> getRndSottoSezioniAll(@RequestParam String idSezione, @RequestParam String idTipoMovimento) throws AssoServiceException{
        return rendicontoDAO.getRndSottoSezioniAll(idSezione, idTipoMovimento);
    }

    // ... (Altri metodi GET per Rendiconto lasciati nel Controller)

    @RequestMapping(value = "/getRendiconto.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody Rendiconto getRendiconto(@RequestParam String sottoVoce) throws AssoServiceException{
        return rendicontoDAO.getRendicontoBySottoVoce(sottoVoce);
    }

    // Voci di Movimento
    @RequestMapping(value = "/getVociMovimentoBySearch.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<VoceMovimento> getVociMovimentoBySearch(@RequestParam String search) throws AssoServiceException{
        return movimentiDao.getVociMovimentoBySearch(search);
    }

    @RequestMapping(value = "/getVociMovimento.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<VoceMovimento> getVociMovimento() throws AssoServiceException{
        List<VoceMovimento> vm = movimentiDao.getAllVoceMovimento();
        for(VoceMovimento m : vm) {
            m.setRendiconto(rendicontoDAO.getRendicontoBySottoVoce(m.getId_cr_sotto_voce()));
        }
        return vm;
    }

    // ... (Altri metodi CRUD per Voci Movimento)

    @RequestMapping(value = "/saveOrUpdateVoceMovimento.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateVoceMovimento(@RequestBody VoceMovimento vf) throws AssoServiceException{
        String result = movimentiDao.saveOrUpdateVoceMovimento(vf);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/eliminaVoceMovimento.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> eliminaVoceMovimento(@RequestBody VoceMovimento vf) throws AssoServiceException{
        String result = movimentiDao.deleteVoceMovimentoByID(vf.getId_vm());
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // Destinazioni
    @RequestMapping(value = "/getDestinazioni.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Destinazione> getDestinazioni() throws AssoServiceException{
        return movimentiDao.getDestinazioni();
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI REPORT (VOID)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/rendiconto.html", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public void exportRendiconto(HttpServletRequest request, HttpServletResponse response, @RequestParam String anno) throws AssoServiceException, IOException, SQLException {
        try {
            simpleReportFiller.setReportFileName(pathReport + "rendiconto.jrxml");
            simpleReportFiller.compileReport();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("anno", anno);

            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            SimpleReportExporter simpleExporter = new SimpleReportExporter();
            simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());
            simpleExporter.exportPdf(response, "rendiconto_" + anno);
        }catch(Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/ricevutaDonazione.html", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public void ricevutaDonazione(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) throws AssoServiceException {
        try {
            String url = request.getRequestURL().toString();
            String tenant = url.substring(url.indexOf("//") + 2, url.indexOf("."));

            simpleReportFiller.setReportFileName(pathReport + "ricevutaDonazione.jrxml");
            simpleReportFiller.compileReport();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", id);
            parameters.put("logo_url", pathPrivateAssets + tenant + "/logo.png");
            parameters.put("firma_url", pathPrivateAssets + tenant + "/firmaPresidente.png");

            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            SimpleReportExporter simpleExporter = new SimpleReportExporter();
            simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());
            simpleExporter.exportPdf(response, "ricevuta");
        }catch(Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }
}