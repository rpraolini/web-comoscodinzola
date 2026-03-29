package it.asso.core.controller.questionario;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.controller.report.SimpleReportExporter;
import it.asso.core.controller.report.SimpleReportFiller;
import it.asso.core.model.animali.gestione.Iter;
import it.asso.core.model.animali.gestione.IterCompleto;
import it.asso.core.model.organizzazione.Organizzazione;
import it.asso.core.model.questionario.QuestionarioSezioni;
import it.asso.core.security.UserAuth;
import it.asso.core.service.QuestionarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.management.JMRuntimeException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jsp") // Path radice per coprire /jsp/public e /jsp/private
public class QuestionarioController {

    private final QuestionarioService questionarioService;
    private final SimpleReportFiller simpleReportFiller;

    // Configurazione (mantenute per i report)
    private final String pathReport;
    private final String pathPrivateAssets;

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String AUTH = "isAuthenticated()";


    // COSTRUTTORE CON INIEZIONE DEL SERVICE E DEI COMPONENTI REPORT
    public QuestionarioController(QuestionarioService questionarioService, SimpleReportFiller simpleReportFiller,
                                  @Value("${path_report}") String pathReport, @Value("${path_private_assets}") String pathPrivateAssets) {
        this.questionarioService = questionarioService;
        this.simpleReportFiller = simpleReportFiller;
        this.pathReport = pathReport;
        this.pathPrivateAssets = pathPrivateAssets;
    }

    // ------------------------------------------------------------------------
    // ENDPOINT PUBBLICI (LETTURA E SCRITTURA ANONIMA)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/public/questionario/getRichiesta.json", method = RequestMethod.GET)
    public @ResponseBody Iter getRichiestaByKey(@RequestParam String key) throws AssoServiceException {
        // Logica di popolamento Contatto spostata nel Service
        return questionarioService.getRichiestaByKey(key);
    }

    @RequestMapping(value = "/public/questionario/getQuestionario.json", method = RequestMethod.GET)
    public @ResponseBody List<QuestionarioSezioni> getQuestionarioByIdRichiesta(@RequestParam String id) throws AssoServiceException {
        // Logica di popolamento Sezioni/Questionari è nel Service
        return questionarioService.getQuestionarioSezioni(id);
    }

    @RequestMapping(value = "/public/questionario/saveOrUpdate.json", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> save(@RequestBody List<QuestionarioSezioni> sezioni) throws AssoServiceException {
        // Logica di salvataggio iterata è nel Service
        questionarioService.saveQuestionarioSezioni(sezioni);
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    @RequestMapping(value = "/public/questionario/inviaQuestionario.json", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> inviaQuestionario(@RequestBody IterCompleto richiestaCompleta) throws AssoServiceException {
        // Logica di salvataggio, invio mail, e aggiornamento stato è nel Service
        questionarioService.inviaQuestionario(richiestaCompleta);
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    // ------------------------------------------------------------------------
    // ENDPOINT PRIVATI (REPORT PDF)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/private/questionario/exportPdf.html", method = RequestMethod.GET)
    @PreAuthorize(AUTH) // Sostituisce checkAuthentication()
    public void exportPdf(HttpServletRequest request, HttpServletResponse response, @RequestParam String key, @AuthenticationPrincipal UserAuth user) throws IOException, SQLException, JMRuntimeException, AssoServiceException {
        try {
            // Risoluzione tenant in modo moderno
            String tenant = user.getUtente().getOrganizzazione().getTenant();

            // **ATTENZIONE:** Il codice originale ricompila due volte lo stesso report.
            // Assumiamo che la versione .jrxml non compilata sia corretta.

            // 1. Preparazione Report (Sotto-report e Main report)
            simpleReportFiller.setReportFileName(pathReport + "questionario_dettaglio.jrxml");
            simpleReportFiller.compileReport();
            simpleReportFiller.setReportFileName(pathReport + "questionario.jrxml");
            simpleReportFiller.compileReport();

            // 2. Preparazione Parametri
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("key", key);
            parameters.put("sotto_report", pathReport);
            parameters.put("logo_url", pathPrivateAssets + "/" + tenant + "/logo.png");

            // Risoluzione Organizzazione (Logica spostata nel Service/Util)
            // L'accesso a request.getSession() deve essere rimosso.
            Organizzazione org = questionarioService.getOrganizzazioneByTenant(tenant);
            parameters.put("rag_sociale", org.getRag_sociale() + " " + org.getSigla_tipo_organizzazione());
            parameters.put("iscrizione", org.getIscrizione());

            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            // 3. Export PDF
            SimpleReportExporter simpleExporter = new SimpleReportExporter();
            simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());
            simpleExporter.exportPdf(response,"questionario");

        }catch(Exception e) {
            // Gestione dell'errore (JMRuntimeException viene gestita da Spring se non catturata)
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }
}