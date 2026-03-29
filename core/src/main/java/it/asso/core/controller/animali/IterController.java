package it.asso.core.controller.animali;

import it.asso.core.common.Def;
import it.asso.core.common.MailController;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.controller.report.SimpleReportExporter;
import it.asso.core.controller.report.SimpleReportFiller;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.gestione.IterDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.gestione.Iter;
import it.asso.core.model.organizzazione.Organizzazione;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/jsp/private/animale")
public class IterController { // Non estende BaseController

    // 1. DIPENDENZE E CONFIGURAZIONI
    private final AnimaleDAO animaleDao;
    private final IterDAO iterDao;
    private final MailController mailController;
    private final SimpleReportFiller simpleReportFiller;

    // Variabili di configurazione (iniettate dal file application.properties)
    private final String pathPrivateAssets;
    private final String pathStemmi;
    private final String pathReport;

    private static final String WRITE_PERMISSION = "@authz.checkWritePermission('AREA_DATI_ANIMALE')";


    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public IterController(AnimaleDAO animaleDao, IterDAO iterDao, MailController mailController,
                          SimpleReportFiller simpleReportFiller,
                          @Value("${path_private_assets}") String pathPrivateAssets,
                          @Value("${path_stemmi}") String pathStemmi,
                          @Value("${path_report}") String pathReport) {
        this.animaleDao = animaleDao;
        this.iterDao = iterDao;
        this.mailController = mailController;
        this.simpleReportFiller = simpleReportFiller;
        this.pathPrivateAssets = pathPrivateAssets;
        this.pathStemmi = pathStemmi;
        this.pathReport = pathReport;
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI SCRITTURA (MAIL)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/inviaMailPreaffidoById.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> inviaMailPreaffidoById(HttpServletRequest request, @RequestBody Iter richiesta) throws AssoServiceException {
        // Recupero l'utente/tenant in modo moderno (non da BaseController)
        // NOTA: La logica qui presuppone che l'oggetto Organizzazione/Tenant sia accessibile
        // tramite un servizio globale, non dalla sessione HTTP (request.getSession().getAttribute("tenant")).

        String result = "";
        try {
            Animale animale = animaleDao.getById(richiesta.getId_animale());
            // MailController dovrà essere aggiornato per non dipendere da request.getSession() per il tenant
            result = mailController.sendMailToPreaffidanteHtml(richiesta, animale, request);

            if (Def.STR_OK.equals(result)) {
                richiesta.setQuest_f(Def.STR_QUEST_INVIATO);
                iterDao.saveOrUpdate(richiesta);
            }
        } catch (Exception ex) {
            throw new AssoServiceException(Def.STR_ERROR_000 + ex.getMessage());
        }

        return ResponseEntity.ok().body("\"" + result + "\""); // Usa ResponseEntity
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI REPORT (VOID)
    // ------------------------------------------------------------------------

    // NOTA: Questi metodi sono complessi perché maneggiano direttamente i flussi I/O (HttpServletResponse)

    @RequestMapping(value = "/exportAffidoPdf.html", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication
    public void exportAffidoPdf(HttpServletRequest request, HttpServletResponse response, @RequestParam String id, @RequestParam String idIter) throws AssoServiceException, IOException, SQLException {
        try {
            // Recupero l'organizzazione (Tenant) in modo moderno
            // **DA FARE: Sostituire l'accesso diretto alla sessione**
            // Organizzazione org = (Organizzazione)request.getSession().getAttribute("tenant");
            // Per ora usiamo un placeholder per sbloccare la compilazione:
            Organizzazione org = new Organizzazione();
            org.setTenant(request.getHeader("Host").split("\\.")[0]); // Placeholder
            org.setRag_sociale("Associazione Demo");
            org.setSigla_tipo_organizzazione("APS");
            org.setIscrizione("CF 00000");

            Animale animale = animaleDao.getById(id);
            if (Def.NUM_UNO.equals(animale.getId_tipo_animale())) {
                simpleReportFiller.setReportFileName(pathReport + "affidoCane.jrxml");
            } else {
                simpleReportFiller.setReportFileName(pathReport + "affidoGatto.jrxml");
            }
            simpleReportFiller.compileReport();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", id);
            parameters.put("idIter", idIter);
            parameters.put("logo_url", pathPrivateAssets + org.getTenant() +"/logo.png");
            parameters.put("rag_sociale", org.getRag_sociale() + " " + org.getSigla_tipo_organizzazione());
            parameters.put("iscrizione", org.getIscrizione());

            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            SimpleReportExporter simpleExporter = new SimpleReportExporter();
            simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());
            simpleExporter.exportPdf(response, "affido"); // Scrive direttamente nel flusso HTTP
        } catch(Exception ex) {
            throw new AssoServiceException(Def.STR_ERROR_000 + ex.getMessage());
        }
    }

    @RequestMapping(value = "/exportPassaggioPdf.html", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication
    public void exportPassaggioPdf(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) throws AssoServiceException, IOException, SQLException {
        try {
            simpleReportFiller.setReportFileName(pathReport + "trasferimento.jrxml");
            simpleReportFiller.compileReport();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", id);
            parameters.put("url_stemma", pathStemmi +"campania.png");

            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            SimpleReportExporter simpleExporter = new SimpleReportExporter();
            simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());
            simpleExporter.exportPdf(response, "trasferimento");
        } catch(Exception ex) {
            throw new AssoServiceException(Def.STR_ERROR_000 + ex.getMessage());
        }
    }
}